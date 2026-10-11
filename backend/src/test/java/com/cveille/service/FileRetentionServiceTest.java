package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.EnrichedCve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileRetentionServiceTest {

    @TempDir
    Path tempDir;

    private FileRetentionService fileRetentionService;

    @BeforeEach
    void setUp() {
        CveilleProperties properties = new CveilleProperties(
                48, tempDir.toString(), null, null,
                new CveilleProperties.Retention(15, 30),
                null, null, null, null, null
        );
        fileRetentionService = new FileRetentionService(properties);
    }

    @Test
    void shouldPruneOldCvesFromIndexMapWhilePreservingRecentAndKev() {
        LocalDate refDate = LocalDate.of(2026, 10, 11);

        // 1. Recent CVE (10 days old) -> keep
        EnrichedCve recentCve = new EnrichedCve(
                "CVE-2026-RECENT", "Desc", "2026-10-01T10:00:00Z", "2026-10-01",
                8.0, "HIGH", false, null, 0.01, 0.5, List.of("java"), false, null
        );

        // 2. Old CVE (40 days old, not in KEV) -> prune
        EnrichedCve oldCve = new EnrichedCve(
                "CVE-2026-OLD", "Desc", "2026-09-01T10:00:00Z", "2026-09-01",
                7.5, "HIGH", false, null, 0.01, 0.5, List.of("docker"), false, null
        );

        // 3. Old CVE (50 days old, but IN CISA KEV) -> keep
        EnrichedCve oldKevCve = new EnrichedCve(
                "CVE-2026-OLD-KEV", "Desc", "2026-08-20T10:00:00Z", "2026-08-20",
                9.8, "CRITICAL", true, null, 0.85, 0.99, List.of("spring"), true, null
        );

        Map<String, EnrichedCve> cveMap = new HashMap<>();
        cveMap.put(recentCve.id(), recentCve);
        cveMap.put(oldCve.id(), oldCve);
        cveMap.put(oldKevCve.id(), oldKevCve);

        fileRetentionService.pruneOldIndexCves(cveMap, refDate);

        assertThat(cveMap).containsOnlyKeys("CVE-2026-RECENT", "CVE-2026-OLD-KEV");
        assertThat(cveMap).doesNotContainKey("CVE-2026-OLD");
    }

    @Test
    void shouldPurgeDailyFilesExceedingLimit() throws IOException {
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);

        for (int i = 1; i <= 20; i++) {
            String day = String.format("%02d", i);
            Files.writeString(dataDir.resolve("2026-09-" + day + ".json"), "{}");
        }

        fileRetentionService.enforceRetention(dataDir);

        try (var stream = Files.list(dataDir)) {
            List<String> remaining = stream.map(p -> p.getFileName().toString()).toList();
            assertThat(remaining).hasSize(15);
            // 2026-09-01 to 2026-09-05 should be purged
            assertThat(remaining).doesNotContain("2026-09-01.json", "2026-09-05.json");
            assertThat(remaining).contains("2026-09-20.json", "2026-09-06.json");
        }
    }

    @Test
    void shouldParsePublishedDateFormatsRobustly() {
        assertThat(FileRetentionService.parsePublishedDate("2026-10-11T16:30:00.123"))
                .isEqualTo(LocalDate.of(2026, 10, 11));
        assertThat(FileRetentionService.parsePublishedDate("2026-10-11"))
                .isEqualTo(LocalDate.of(2026, 10, 11));
        assertThat(FileRetentionService.parsePublishedDate("2026-10-11Z"))
                .isEqualTo(LocalDate.of(2026, 10, 11));
        assertThat(FileRetentionService.parsePublishedDate(null)).isNull();
        assertThat(FileRetentionService.parsePublishedDate("invalid")).isNull();
    }
}
