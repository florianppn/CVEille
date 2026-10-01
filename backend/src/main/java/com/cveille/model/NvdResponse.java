package com.cveille.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NvdResponse(
        Integer resultsPerPage,
        Integer startIndex,
        Integer totalResults,
        String format,
        String version,
        String timestamp,
        List<DefCveItem> vulnerabilities
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DefCveItem(
            CveItem cve
    ) {}
}
