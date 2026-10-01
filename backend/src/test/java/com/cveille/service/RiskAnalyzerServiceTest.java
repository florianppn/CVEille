package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveItem;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.EpssItem;
import com.cveille.model.KevItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RiskAnalyzerServiceTest {

    private RiskAnalyzerService riskAnalyzerService;

    @BeforeEach
    void setUp() {
        CveilleProperties.Alert alert = new CveilleProperties.Alert(7.5, true, 0.20);
        CveilleProperties properties = new CveilleProperties(
                48, "../data", "../frontend", "../README.md", null, alert, null, null, null, null
        );
        riskAnalyzerService = new RiskAnalyzerService(properties);
    }

    @Test
    void shouldTriggerAlertWhenInCisaKev() {
        boolean isAlert = riskAnalyzerService.evaluateAlert(5.0, true, 0.05);
        assertThat(isAlert).isTrue();
    }

    @Test
    void shouldTriggerAlertWhenCvssIsHigh() {
        boolean isAlert = riskAnalyzerService.evaluateAlert(9.8, false, 0.01);
        assertThat(isAlert).isTrue();
    }

    @Test
    void shouldTriggerAlertWhenEpssIsHigh() {
        boolean isAlert = riskAnalyzerService.evaluateAlert(6.0, false, 0.45);
        assertThat(isAlert).isTrue();
    }

    @Test
    void shouldNotTriggerAlertWhenBelowThresholds() {
        boolean isAlert = riskAnalyzerService.evaluateAlert(5.3, false, 0.02);
        assertThat(isAlert).isFalse();
    }

    @Test
    void shouldEnrichCveWithAllThreatIntelSources() {
        CveItem.CvssData cvssData = new CveItem.CvssData("3.1", "CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H", 9.8, "CRITICAL");
        CveItem.CvssMetricV31 metric = new CveItem.CvssMetricV31("Primary", cvssData);
        CveItem.Metrics metrics = new CveItem.Metrics(List.of(metric), null, null);
        CveItem.Description desc = new CveItem.Description("en", "Critical flaw in Spring Boot security filter");
        CveItem cve = new CveItem("CVE-2024-1234", "nvd", "2024-02-01", "2024-02-02", "Analyzed",
                List.of(desc), metrics, null);

        KevItem kev = new KevItem("CVE-2024-1234", "Spring", "Spring Boot", "Auth Bypass",
                "2024-02-05", "Short desc", "Update immediately", "2024-02-20", "Known", "Notes");
        EpssItem epss = new EpssItem("CVE-2024-1234", "0.75000", "0.98000", "2024-02-06");

        EnrichedCve enriched = riskAnalyzerService.enrich(cve, Optional.of(kev), Optional.of(epss), List.of("spring-boot"));

        assertThat(enriched.id()).isEqualTo("CVE-2024-1234");
        assertThat(enriched.cvssScore()).isEqualTo(9.8);
        assertThat(enriched.severity()).isEqualTo("CRITICAL");
        assertThat(enriched.inKev()).isTrue();
        assertThat(enriched.epssScore()).isEqualTo(0.75);
        assertThat(enriched.epssPercentile()).isEqualTo(0.98);
        assertThat(enriched.isAlert()).isTrue();
        assertThat(enriched.nvdUrl()).contains("CVE-2024-1234");
    }

    @Test
    void shouldComputeAccurateStats() {
        EnrichedCve cve1 = new EnrichedCve("CVE-1", "desc1", "2024-01-01", "2024-01-02",
                9.8, "CRITICAL", true, null, 0.8, 0.99, List.of("spring"), true, "url1");
        EnrichedCve cve2 = new EnrichedCve("CVE-2", "desc2", "2024-01-01", "2024-01-02",
                7.5, "HIGH", false, null, 0.1, 0.50, List.of("angular"), true, "url2");
        EnrichedCve cve3 = new EnrichedCve("CVE-3", "desc3", "2024-01-01", "2024-01-02",
                5.0, "MEDIUM", false, null, 0.02, 0.20, List.of("docker"), false, "url3");

        CveStats stats = riskAnalyzerService.computeStats(List.of(cve1, cve2, cve3));

        assertThat(stats.totalTracked()).isEqualTo(3);
        assertThat(stats.criticalCount()).isEqualTo(1);
        assertThat(stats.highCount()).isEqualTo(1);
        assertThat(stats.mediumCount()).isEqualTo(1);
        assertThat(stats.kevCount()).isEqualTo(1);
        assertThat(stats.alertCount()).isEqualTo(2);
        assertThat(stats.topTechnologies()).containsEntry("spring", 1);
        assertThat(stats.topTechnologies()).containsEntry("angular", 1);
    }
}
