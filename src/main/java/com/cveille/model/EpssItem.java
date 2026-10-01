package com.cveille.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EpssItem(
        String cve,
        String epss,
        String percentile,
        String date
) {
    public Double getEpssScore() {
        if (epss == null || epss.isBlank()) return null;
        try {
            return Double.parseDouble(epss);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getPercentileScore() {
        if (percentile == null || percentile.isBlank()) return null;
        try {
            return Double.parseDouble(percentile);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
