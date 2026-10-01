package com.cveille.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KevCatalog(
        String title,
        String catalogVersion,
        String dateReleased,
        int count,
        List<KevItem> vulnerabilities
) {}
