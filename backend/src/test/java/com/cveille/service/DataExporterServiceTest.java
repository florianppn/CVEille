package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataExporterServiceTest {

    @TempDir
    Path tempDir;

    private DataExporterService dataExporterService;
    private Path readmePath;
    private Path dataDir;

    @BeforeEach
    void setUp() throws IOException {
        dataDir = tempDir.resolve("data");
        readmePath = tempDir.resolve("README.md");

        Files.writeString(readmePath, "# Project Title\n\nSome introductory text.\n\n<!-- CVEILLE_SUMMARY_START -->\nold\n<!-- CVEILLE_SUMMARY_END -->\n\nFooter.");

        CveilleProperties properties = new CveilleProperties(
                48, dataDir.toString(), readmePath.toString(), null, null, null, null, null, null
        );
        dataExporterService = new DataExporterService(properties);
    }

    @Test
    void shouldExportJsonFilesAndInjectDynamicReadmeSection() throws IOException {
        EnrichedCve cve = new EnrichedCve(
                "CVE-2024-5555",
                "Critical remote code execution in Spring Core",
                "2024-03-01",
                "2024-03-02",
                9.8,
                "CRITICAL",
                true,
                null,
                0.85,
                0.99,
                List.of("spring", "spring-boot"),
                true,
                "https://nvd.nist.gov/vuln/detail/CVE-2024-5555"
        );

        CveStats stats = new CveStats(
                1, 1, 0, 0, 0, 1, 1, 0.85,
                Map.of("CRITICAL", 1, "HIGH", 0, "MEDIUM", 0, "LOW", 0),
                Map.of("spring", 1),
                "2024-03-02T12:00:00Z"
        );

        SyncSummary summary = new SyncSummary(
                "2024-03-02", 50, 1, 1, stats, List.of(cve), List.of(cve)
        );

        boolean hasNewData = dataExporterService.exportAll(summary);

        assertThat(hasNewData).isTrue();

        Path indexFile = dataDir.resolve("index.json");
        assertThat(Files.exists(indexFile)).isTrue();
        String indexContent = Files.readString(indexFile);
        assertThat(indexContent).contains("CVE-2024-5555");
        assertThat(indexContent).contains("CRITICAL");

        // Verify README update
        String readmeContent = Files.readString(readmePath);
        assertThat(readmeContent).contains("CVE-2024-5555");
        assertThat(readmeContent).contains("Exploits CISA KEV");
        assertThat(readmeContent).contains("Some introductory text.");
        assertThat(readmeContent).contains("Footer.");
    }
}
