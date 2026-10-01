package com.cveille.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EpssResponse(
        String status,
        Integer statusCode,
        Integer total,
        List<EpssItem> data
) {}
