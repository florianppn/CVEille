package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class RiskAnalyzerService {

    private final CveilleProperties properties;

    public RiskAnalyzerService(CveilleProperties properties) {
        this.properties = properties;
    }

    /**
     * Enriches a CveItem with KEV, EPSS and stack detection results.
     */
    public EnrichedCve enrich(CveItem cve,
                              Optional<KevItem> kevOpt,
                              Optional<EpssItem> epssOpt,
                              List<String> matchedKeywords) {
        Double cvss = cve.getCvssScore();
        String severity = cve.getSeverity();

        boolean inKev = kevOpt.isPresent();
        EnrichedCve.KevDetail kevDetail = null;
        if (inKev) {
            KevItem k = kevOpt.get();
            kevDetail = new EnrichedCve.KevDetail(
                    k.vendorProject(),
                    k.product(),
                    k.vulnerabilityName(),
                    k.dateAdded(),
                    k.requiredAction(),
                    k.knownRansomwareCampaignUse()
            );
        }

        Double epssScore = epssOpt.map(EpssItem::getEpssScore).orElse(null);
        Double epssPercentile = epssOpt.map(EpssItem::getPercentileScore).orElse(null);

        boolean isAlert = evaluateAlert(cvss, inKev, epssScore);

        return new EnrichedCve(
                cve.id(),
                cve.getEnglishDescription(),
                cve.published(),
                cve.lastModified(),
                cvss,
                severity,
                inKev,
                kevDetail,
                epssScore,
                epssPercentile,
                matchedKeywords,
                isAlert,
                EnrichedCve.buildNvdUrl(cve.id())
        );
    }

    /**
     * Determines whether the CVE qualifies as a high-risk / critical alert.
     */
    public boolean evaluateAlert(Double cvss, boolean inKev, Double epssScore) {
        if (properties.alert() == null) return false;

        // Condition 1: Actively exploited in CISA KEV
        if (properties.alert().alertOnKev() && inKev) {
            return true;
        }

        // Condition 2: CVSS >= alert threshold (e.g. >= 7.5 or >= 9.0)
        if (cvss != null && cvss >= properties.alert().minCvss()) {
            return true;
        }

        // Condition 3: EPSS probability >= threshold (e.g. >= 0.20)
        if (epssScore != null && epssScore >= properties.alert().minEpss()) {
            return true;
        }

        return false;
    }

    /**
     * Aggregates key metrics and distribution statistics from enriched CVEs.
     */
    public CveStats computeStats(List<EnrichedCve> cves) {
        if (cves == null || cves.isEmpty()) {
            return new CveStats(0, 0, 0, 0, 0, 0, 0, 0.0,
                    Map.of("CRITICAL", 0, "HIGH", 0, "MEDIUM", 0, "LOW", 0),
                    Collections.emptyMap(),
                    Instant.now().toString());
        }

        int total = cves.size();
        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;
        int kev = 0;
        int alerts = 0;
        double epssSum = 0.0;
        int epssCount = 0;

        Map<String, Integer> severityMap = new LinkedHashMap<>();
        severityMap.put("CRITICAL", 0);
        severityMap.put("HIGH", 0);
        severityMap.put("MEDIUM", 0);
        severityMap.put("LOW", 0);

        Map<String, Integer> techMap = new HashMap<>();

        for (EnrichedCve cve : cves) {
            String sev = cve.severity() != null ? cve.severity().toUpperCase() : "UNKNOWN";
            switch (sev) {
                case "CRITICAL" -> {
                    critical++;
                    severityMap.put("CRITICAL", severityMap.get("CRITICAL") + 1);
                }
                case "HIGH" -> {
                    high++;
                    severityMap.put("HIGH", severityMap.get("HIGH") + 1);
                }
                case "MEDIUM" -> {
                    medium++;
                    severityMap.put("MEDIUM", severityMap.get("MEDIUM") + 1);
                }
                case "LOW" -> {
                    low++;
                    severityMap.put("LOW", severityMap.get("LOW") + 1);
                }
                default -> {
                    // map to low or unassigned
                }
            }

            if (cve.inKev()) kev++;
            if (cve.isAlert()) alerts++;

            if (cve.epssScore() != null) {
                epssSum += cve.epssScore();
                epssCount++;
            }

            if (cve.matchedKeywords() != null) {
                for (String kw : cve.matchedKeywords()) {
                    techMap.put(kw, techMap.getOrDefault(kw, 0) + 1);
                }
            }
        }

        double avgEpss = epssCount > 0 ? (epssSum / epssCount) : 0.0;

        // Sort top technologies by count descending
        Map<String, Integer> sortedTech = new LinkedHashMap<>();
        techMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> sortedTech.put(e.getKey(), e.getValue()));

        return new CveStats(
                total,
                critical,
                high,
                medium,
                low,
                kev,
                alerts,
                Math.round(avgEpss * 1000.0) / 1000.0,
                severityMap,
                sortedTech,
                Instant.now().toString()
        );
    }
}
