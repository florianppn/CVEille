package com.cveille.service;

import com.cveille.client.CisaKevClient;
import com.cveille.client.EpssClient;
import com.cveille.client.NvdClient;
import com.cveille.config.CveilleProperties;
import com.cveille.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CveSyncService {

    private static final Logger log = LoggerFactory.getLogger(CveSyncService.class);

    private final CveilleProperties properties;
    private final NvdClient nvdClient;
    private final CisaKevClient cisaKevClient;
    private final EpssClient epssClient;
    private final StackFilterService stackFilterService;
    private final RiskAnalyzerService riskAnalyzerService;
    private final DataExporterService dataExporterService;

    public CveSyncService(CveilleProperties properties,
                          NvdClient nvdClient,
                          CisaKevClient cisaKevClient,
                          EpssClient epssClient,
                          StackFilterService stackFilterService,
                          RiskAnalyzerService riskAnalyzerService,
                          DataExporterService dataExporterService) {
        this.properties = properties;
        this.nvdClient = nvdClient;
        this.cisaKevClient = cisaKevClient;
        this.epssClient = epssClient;
        this.stackFilterService = stackFilterService;
        this.riskAnalyzerService = riskAnalyzerService;
        this.dataExporterService = dataExporterService;
    }

    /**
     * Executes the end-to-end CVE threat intelligence pipeline.
     */
    public SyncSummary runSync() {
        log.info("=== Starting CVEille Threat Intelligence Sync ===");

        // 1. Fetch recent CVEs from NVD
        int windowHours = properties.windowHours() > 0 ? properties.windowHours() : 48;
        List<CveItem> rawCves = nvdClient.fetchRecentCves(windowHours);
        log.info("Step 1/6: Received {} CVEs from NVD API 2.0", rawCves.size());

        // 2. Preload CISA KEV catalog
        cisaKevClient.loadCatalog();
        log.info("Step 2/6: CISA KEV catalog indexed ({} entries)", cisaKevClient.getCachedCount());

        // 3. Filter CVEs matching monitored stack (Spring Boot & Angular + infra)
        List<CveItem> matchedCves = new ArrayList<>();
        Map<String, List<String>> cveKeywordsMap = new HashMap<>();

        for (CveItem cve : rawCves) {
            List<String> keywords = stackFilterService.findMatchedKeywords(cve);
            if (!keywords.isEmpty()) {
                matchedCves.add(cve);
                cveKeywordsMap.put(cve.id(), keywords);
            }
        }
        log.info("Step 3/6: Filtered {} CVEs matching enterprise stack out of {}", matchedCves.size(), rawCves.size());

        // 4. Batch query EPSS scores for matched CVEs
        List<String> matchedIds = matchedCves.stream().map(CveItem::id).toList();
        Map<String, EpssItem> epssMap = epssClient.fetchEpssScores(matchedIds);
        log.info("Step 4/6: Retrieved EPSS scores for {} CVEs", epssMap.size());

        // 5. Enrich and evaluate risk
        List<EnrichedCve> enrichedList = new ArrayList<>();
        List<EnrichedCve> alertList = new ArrayList<>();

        for (CveItem cve : matchedCves) {
            Optional<KevItem> kevOpt = cisaKevClient.findKev(cve.id());
            Optional<EpssItem> epssOpt = Optional.ofNullable(epssMap.get(cve.id().toUpperCase()));
            List<String> keywords = cveKeywordsMap.getOrDefault(cve.id(), Collections.emptyList());

            EnrichedCve enriched = riskAnalyzerService.enrich(cve, kevOpt, epssOpt, keywords);
            enrichedList.add(enriched);

            if (enriched.isAlert()) {
                alertList.add(enriched);
            }
        }

        CveStats stats = riskAnalyzerService.computeStats(enrichedList);
        String todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        SyncSummary summary = new SyncSummary(
                todayStr,
                rawCves.size(),
                matchedCves.size(),
                enrichedList.size(),
                stats,
                enrichedList,
                alertList
        );

        // 6. Export data
        log.info("Step 5/5: Exporting data to JSON and updating README...");
        boolean hasNewData = dataExporterService.exportAll(summary);

        log.info("=== CVEille Sync Completed. New Data Saved: {} ===", hasNewData);
        return summary;
    }
}
