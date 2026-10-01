package com.cveille.model;

import java.util.Map;

public record CveStats(
        int totalTracked,
        int criticalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        int kevCount,
        int alertCount,
        double avgEpss,
        Map<String, Integer> severityDistribution,
        Map<String, Integer> topTechnologies,
        String lastSyncTimestamp
) {}
