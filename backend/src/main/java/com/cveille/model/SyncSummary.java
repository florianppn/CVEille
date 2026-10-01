package com.cveille.model;

import java.util.List;

public record SyncSummary(
        String syncDate,
        int totalNvdFetched,
        int totalMatchedStack,
        int newCvesCount,
        CveStats stats,
        List<EnrichedCve> cves,
        List<EnrichedCve> alerts
) {}
