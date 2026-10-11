package com.cveille.service;

import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;
import com.cveille.repository.CveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service that coordinates exporting and persisting CVE sync results.
 * Delegates storage to CveRepository, retention to FileRetentionService, and docs to ReadmeSyncService.
 */
@Service
public class DataExporterService {

    private static final Logger log = LoggerFactory.getLogger(DataExporterService.class);

    private final CveRepository cveRepository;
    private final RiskAnalyzerService riskAnalyzerService;
    private final FileRetentionService fileRetentionService;
    private final ReadmeSyncService readmeSyncService;

    public DataExporterService(CveRepository cveRepository,
                               RiskAnalyzerService riskAnalyzerService,
                               FileRetentionService fileRetentionService,
                               ReadmeSyncService readmeSyncService) {
        this.cveRepository = cveRepository;
        this.riskAnalyzerService = riskAnalyzerService;
        this.fileRetentionService = fileRetentionService;
        this.readmeSyncService = readmeSyncService;
    }

    /**
     * Exports the daily JSON archive, updates index.json, mirrors to frontend, purges old files, and updates README.md.
     * Returns true if any new data was saved.
     */
    public boolean exportAll(SyncSummary summary) {
        try {
            String dateStr = (summary != null && summary.syncDate() != null && !summary.syncDate().isBlank())
                    ? summary.syncDate()
                    : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            boolean dailyAlreadyExisted = cveRepository.existsDaily(dateStr);

            // 1. Read existing index from repository
            Map<String, EnrichedCve> allCvesMap = cveRepository.findAllAsMap();
            int initialCount = allCvesMap.size();

            // 2. Merge today's synced CVEs
            if (summary != null && summary.cves() != null) {
                for (EnrichedCve cve : summary.cves()) {
                    allCvesMap.put(cve.id(), cve);
                }
            }

            int finalCount = allCvesMap.size();
            boolean hasNewData = (finalCount > initialCount) || !dailyAlreadyExisted;

            // 3. Save daily file archive
            cveRepository.saveDaily(dateStr, summary);

            // 4. Enforce rolling index retention (e.g. 30 days)
            LocalDate referenceDate = LocalDate.parse(dateStr);
            if (fileRetentionService != null) {
                fileRetentionService.pruneOldIndexCves(allCvesMap, referenceDate);
            }

            // 5. Sort aggregated CVEs (KEV first, then CVSS descending)
            List<EnrichedCve> sortedList = new ArrayList<>(allCvesMap.values());
            sortedList.sort((a, b) -> {
                if (a.inKev() != b.inKev()) return Boolean.compare(b.inKev(), a.inKev());
                Double cvssA = a.cvssScore() != null ? a.cvssScore() : 0.0;
                Double cvssB = b.cvssScore() != null ? b.cvssScore() : 0.0;
                return Double.compare(cvssB, cvssA);
            });

            // 5. Compute aggregated stats across all accumulated CVEs
            CveStats aggregatedStats = riskAnalyzerService != null
                    ? riskAnalyzerService.computeStats(sortedList)
                    : summary.stats();

            // 6. Save aggregated index (both data/ and frontend/)
            cveRepository.saveIndex(sortedList, aggregatedStats, summary.newCvesCount(), summary.stats().lastSyncTimestamp());

            // 7. Enforce file retention policy
            fileRetentionService.enforceRetention();

            // 8. Update README.md dynamic sections
            readmeSyncService.updateReadme(aggregatedStats, sortedList);

            return hasNewData;
        } catch (Exception e) {
            log.error("Failed to export CVE data: {}", e.getMessage(), e);
            return false;
        }
    }
}
