package com.cveille.repository;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileCveRepositoryTest {

    @TempDir
    Path tempDir;

    private FileCveRepository repository;
    private Path dataDir;
    private Path frontendDir;

    @BeforeEach
    void setUp() {
        dataDir = tempDir.resolve("data");
        frontendDir = tempDir.resolve("frontend");
        CveilleProperties properties = new CveilleProperties(
                48, dataDir.toString(), frontendDir.toString(), null, new CveilleProperties.Retention(15), null, null, null, null, null
        );
        repository = new FileCveRepository(properties, new ObjectMapper());
    }

    @Test
    void shouldSaveAndQueryCvesWithFilters() throws IOException {
        EnrichedCve cve1 = new EnrichedCve(
                "CVE-2026-0001", "Spring Boot Remote Code Execution", "2026-10-10", "2026-10-10",
                9.8, "CRITICAL", true, null, 0.5, 0.9, List.of("spring-boot"), true, "https://nvd.nist.gov/1"
        );
        EnrichedCve cve2 = new EnrichedCve(
                "CVE-2026-0002", "Docker privilege escalation", "2026-10-09", "2026-10-09",
                7.5, "HIGH", false, null, 0.1, 0.4, List.of("docker"), true, "https://nvd.nist.gov/2"
        );

        CveStats stats = new CveStats(2, 1, 1, 0, 0, 1, 2, 0.3,
                Map.of("CRITICAL", 1, "HIGH", 1), Map.of("spring-boot", 1, "docker", 1), "2026-10-10T12:00:00Z");

        repository.saveIndex(List.of(cve1, cve2), stats, 2, "2026-10-10T12:00:00Z");

        // 1. Find all
        List<EnrichedCve> all = repository.findAll();
        assertThat(all).hasSize(2);

        // 2. Filter by search
        List<EnrichedCve> springList = repository.findWithFilters("spring", null, null, null);
        assertThat(springList).hasSize(1);
        assertThat(springList.get(0).id()).isEqualTo("CVE-2026-0001");

        // 3. Filter by severity
        List<EnrichedCve> highList = repository.findWithFilters(null, "HIGH", null, null);
        assertThat(highList).hasSize(1);
        assertThat(highList.get(0).id()).isEqualTo("CVE-2026-0002");

        // 4. Filter by KEV only
        List<EnrichedCve> kevList = repository.findWithFilters(null, null, true, null);
        assertThat(kevList).hasSize(1);
        assertThat(kevList.get(0).id()).isEqualTo("CVE-2026-0001");

        // 5. Filter by tech
        List<EnrichedCve> dockerList = repository.findWithFilters(null, null, null, "docker");
        assertThat(dockerList).hasSize(1);
        assertThat(dockerList.get(0).id()).isEqualTo("CVE-2026-0002");
    }
}
