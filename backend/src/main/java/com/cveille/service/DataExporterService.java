package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DataExporterService {

    private static final Logger log = LoggerFactory.getLogger(DataExporterService.class);
    private static final String SUMMARY_START_TAG = "<!-- CVEILLE_SUMMARY_START -->";
    private static final String SUMMARY_END_TAG = "<!-- CVEILLE_SUMMARY_END -->";

    private final CveilleProperties properties;
    private final ObjectMapper objectMapper;

    public DataExporterService(CveilleProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exports the daily JSON file, updates index.json and README.md.
     * Returns true if any new or modified data was saved.
     */
    public boolean exportAll(SyncSummary summary) {
        try {
            Path dataDir = Paths.get(properties.dataDir()).normalize().toAbsolutePath();
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            String todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            Path dailyFile = dataDir.resolve(todayStr + ".json");
            Path indexFile = dataDir.resolve("index.json");

            // 1. Read existing index.json to merge without duplicates
            Map<String, EnrichedCve> allCvesMap = new LinkedHashMap<>();
            if (Files.exists(indexFile)) {
                try {
                    Map<String, Object> existingIndex = objectMapper.readValue(indexFile.toFile(), new TypeReference<>() {});
                    if (existingIndex.containsKey("cves")) {
                        List<Map<String, Object>> rawList = (List<Map<String, Object>>) existingIndex.get("cves");
                        for (Map<String, Object> rawItem : rawList) {
                            EnrichedCve cve = objectMapper.convertValue(rawItem, EnrichedCve.class);
                            if (cve.id() != null) {
                                allCvesMap.put(cve.id(), cve);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not parse existing index.json, creating a new index: {}", e.getMessage());
                }
            }

            int initialCount = allCvesMap.size();

            // 2. Add current CVEs
            if (summary.cves() != null) {
                for (EnrichedCve cve : summary.cves()) {
                    allCvesMap.put(cve.id(), cve);
                }
            }

            int finalCount = allCvesMap.size();
            boolean hasNewData = (finalCount > initialCount) || !Files.exists(dailyFile);

            // 3. Write daily file
            objectMapper.writeValue(dailyFile.toFile(), summary);
            log.info("Wrote daily CVE data to {}", dailyFile);

            // 4. Write aggregated index.json (sorted by date / ID descending)
            List<EnrichedCve> sortedList = new ArrayList<>(allCvesMap.values());
            sortedList.sort((a, b) -> {
                if (a.inKev() != b.inKev()) return Boolean.compare(b.inKev(), a.inKev());
                Double cvssA = a.cvssScore() != null ? a.cvssScore() : 0.0;
                Double cvssB = b.cvssScore() != null ? b.cvssScore() : 0.0;
                return Double.compare(cvssB, cvssA);
            });

            Map<String, Object> indexPayload = new LinkedHashMap<>();
            indexPayload.put("lastSync", summary.stats().lastSyncTimestamp());
            indexPayload.put("stats", summary.stats());
            indexPayload.put("cves", sortedList);

            objectMapper.writeValue(indexFile.toFile(), indexPayload);
            log.info("Updated index.json with {} total unique CVEs", sortedList.size());

            // 4b. Mirror to frontend directory for the GitHub Pages web dashboard
            if (properties.frontendDir() != null && !properties.frontendDir().isBlank()) {
                Path frontendDataDir = Paths.get(properties.frontendDir(), "data").normalize().toAbsolutePath();
                if (!Files.exists(frontendDataDir)) {
                    Files.createDirectories(frontendDataDir);
                }
                Path frontendIndexFile = frontendDataDir.resolve("index.json");
                objectMapper.writeValue(frontendIndexFile.toFile(), indexPayload);
                log.info("Mirrored index.json to frontend at {}", frontendIndexFile);
            }

            // 5. Update README.md dynamic sections
            updateReadme(summary.stats(), sortedList);

            return hasNewData;
        } catch (IOException e) {
            log.error("Failed to export CVE data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Updates README.md between <!-- CVEILLE_SUMMARY_START --> and <!-- CVEILLE_SUMMARY_END -->.
     */
    public void updateReadme(CveStats stats, List<EnrichedCve> cves) {
        Path readmePath = Paths.get(properties.readmePath()).normalize().toAbsolutePath();
        if (!Files.exists(readmePath)) {
            log.warn("README file not found at {}, skipping README update", readmePath);
            return;
        }

        try {
            String content = Files.readString(readmePath);
            String dynamicSection = generateReadmeSection(stats, cves);

            if (content.contains(SUMMARY_START_TAG) && content.contains(SUMMARY_END_TAG)) {
                int start = content.indexOf(SUMMARY_START_TAG);
                int end = content.indexOf(SUMMARY_END_TAG) + SUMMARY_END_TAG.length();

                String updatedContent = content.substring(0, start) + dynamicSection + content.substring(end);
                Files.writeString(readmePath, updatedContent);
                log.info("Successfully updated dynamic section in README.md");
            } else {
                // If tags don't exist yet, append them
                String updatedContent = content + "\n\n" + dynamicSection + "\n";
                Files.writeString(readmePath, updatedContent);
                log.info("Appended dynamic section to README.md");
            }
        } catch (IOException e) {
            log.error("Failed to update README.md: {}", e.getMessage(), e);
        }
    }

    private String generateReadmeSection(CveStats stats, List<EnrichedCve> cves) {
        StringBuilder sb = new StringBuilder();
        sb.append(SUMMARY_START_TAG).append("\n");
        sb.append("### 🛡️ État de la veille CVEille (Stack Spring Boot & Angular)\n\n");
        sb.append("> **Dernière synchronisation** : `").append(stats.lastSyncTimestamp()).append("`  \n");
        sb.append("> **Vulnérabilités suivies** : `").append(stats.totalTracked()).append("` | ");
        sb.append("🔥 **Exploits CISA KEV** : `").append(stats.kevCount()).append("` | ");
        sb.append("🚨 **Critiques** : `").append(stats.criticalCount()).append("` | ");
        sb.append("⚠️ **Élevées** : `").append(stats.highCount()).append("`\n\n");

        sb.append("| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |\n");
        sb.append("|---|---|---|---|---|---|---|\n");

        int limit = Math.min(cves.size(), 8);
        for (int i = 0; i < limit; i++) {
            EnrichedCve cve = cves.get(i);
            String kevBadge = cve.inKev() ? "🔥 **OUI**" : "Non";
            String cvssStr = cve.cvssScore() != null ? String.valueOf(cve.cvssScore()) : "N/A";
            String epssStr = cve.epssScore() != null ? String.format(Locale.US, "%.1f%%", cve.epssScore() * 100) : "N/A";
            String stackTags = cve.matchedKeywords() != null ? String.join(", ", cve.matchedKeywords()) : "";

            String desc = cve.description() != null ? cve.description().replace("\n", " ").trim() : "";
            if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
            desc = desc.replace("|", "\\|");

            sb.append("| [").append(cve.id()).append("](").append(cve.nvdUrl()).append(") ")
              .append("| `").append(cve.severity()).append("` ")
              .append("| ").append(cvssStr).append(" ")
              .append("| ").append(epssStr).append(" ")
              .append("| ").append(kevBadge).append(" ")
              .append("| `").append(stackTags).append("` ")
              .append("| ").append(desc).append(" |\n");
        }

        sb.append("\n*Données détaillées historisées chaque jour dans [`data/`](data/) • Alimenté par NVD 2.0, CISA KEV & EPSS*\n");
        sb.append(SUMMARY_END_TAG);
        return sb.toString();
    }
}
