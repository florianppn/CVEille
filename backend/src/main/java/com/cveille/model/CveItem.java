package com.cveille.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CveItem(
        String id,
        String sourceIdentifier,
        String published,
        String lastModified,
        String vulnStatus,
        List<Description> descriptions,
        Metrics metrics,
        List<Configuration> configurations
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Description(
            String lang,
            String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metrics(
            List<CvssMetricV31> cvssMetricV31,
            List<CvssMetricV30> cvssMetricV30,
            List<CvssMetricV2> cvssMetricV2
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssMetricV31(
            String type,
            CvssData cvssData
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssMetricV30(
            String type,
            CvssData cvssData
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssData(
            String version,
            String vectorString,
            Double baseScore,
            String baseSeverity
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssMetricV2(
            String type,
            CvssV2Data cvssData,
            String baseSeverity
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssV2Data(
            String version,
            String vectorString,
            Double baseScore
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Configuration(
            List<Node> nodes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Node(
            String operator,
            Boolean negate,
            List<CpeMatch> cpeMatch
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CpeMatch(
            Boolean vulnerable,
            String criteria,
            String matchCriteriaId
    ) {}

    /**
     * Returns the best available CVSS score (preferring v3.1, then v3.0, then v2).
     */
    public Double getCvssScore() {
        if (metrics != null) {
            if (metrics.cvssMetricV31() != null && !metrics.cvssMetricV31().isEmpty()) {
                CvssData data = metrics.cvssMetricV31().get(0).cvssData();
                if (data != null && data.baseScore() != null) return data.baseScore();
            }
            if (metrics.cvssMetricV30() != null && !metrics.cvssMetricV30().isEmpty()) {
                CvssData data = metrics.cvssMetricV30().get(0).cvssData();
                if (data != null && data.baseScore() != null) return data.baseScore();
            }
            if (metrics.cvssMetricV2() != null && !metrics.cvssMetricV2().isEmpty()) {
                CvssV2Data data = metrics.cvssMetricV2().get(0).cvssData();
                if (data != null && data.baseScore() != null) return data.baseScore();
            }
        }
        return null;
    }

    /**
     * Returns the CVSS severity string (CRITICAL, HIGH, MEDIUM, LOW) or UNKNOWN.
     */
    public String getSeverity() {
        if (metrics != null) {
            if (metrics.cvssMetricV31() != null && !metrics.cvssMetricV31().isEmpty()) {
                CvssData data = metrics.cvssMetricV31().get(0).cvssData();
                if (data != null && data.baseSeverity() != null) return data.baseSeverity().toUpperCase();
            }
            if (metrics.cvssMetricV30() != null && !metrics.cvssMetricV30().isEmpty()) {
                CvssData data = metrics.cvssMetricV30().get(0).cvssData();
                if (data != null && data.baseSeverity() != null) return data.baseSeverity().toUpperCase();
            }
            if (metrics.cvssMetricV2() != null && !metrics.cvssMetricV2().isEmpty()) {
                String sev = metrics.cvssMetricV2().get(0).baseSeverity();
                if (sev != null) return sev.toUpperCase();
            }
        }
        Double score = getCvssScore();
        if (score != null) {
            if (score >= 9.0) return "CRITICAL";
            if (score >= 7.0) return "HIGH";
            if (score >= 4.0) return "MEDIUM";
            return "LOW";
        }
        return "UNKNOWN";
    }

    /**
     * Returns the English description if available, otherwise the first description.
     */
    public String getEnglishDescription() {
        if (descriptions == null || descriptions.isEmpty()) return "No description available.";
        for (Description desc : descriptions) {
            if ("en".equalsIgnoreCase(desc.lang())) {
                return desc.value();
            }
        }
        return descriptions.get(0).value();
    }
}
