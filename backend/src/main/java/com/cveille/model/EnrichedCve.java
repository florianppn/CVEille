package com.cveille.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnrichedCve(
        String id,
        String description,
        String publishedDate,
        String lastModifiedDate,
        Double cvssScore,
        String severity,
        boolean inKev,
        KevDetail kevDetail,
        Double epssScore,
        Double epssPercentile,
        List<String> matchedKeywords,
        boolean isAlert,
        String nvdUrl
) {
    public record KevDetail(
            String vendorProject,
            String product,
            String vulnerabilityName,
            String dateAdded,
            String requiredAction,
            String knownRansomwareCampaignUse
    ) {}

    public static String buildNvdUrl(String cveId) {
        return "https://nvd.nist.gov/vuln/detail/" + cveId;
    }
}
