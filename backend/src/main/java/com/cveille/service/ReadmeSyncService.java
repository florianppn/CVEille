package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service responsible solely for updating the dynamic CVE intelligence section in README.md.
 */
@Service
public class ReadmeSyncService {

    private static final Logger log = LoggerFactory.getLogger(ReadmeSyncService.class);
    private static final String SUMMARY_START_TAG = "<!-- CVEILLE_SUMMARY_START -->";
    private static final String SUMMARY_END_TAG = "<!-- CVEILLE_SUMMARY_END -->";

    private final CveilleProperties properties;

    public ReadmeSyncService(CveilleProperties properties) {
        this.properties = properties;
    }

    /**
     * Injects the latest vulnerability metrics and top critical/high CVEs into the README.md.
     */
    public void updateReadme(CveStats stats, List<EnrichedCve> topCves) {
        String readmePathStr = properties.readmePath();
        if (readmePathStr == null || readmePathStr.isBlank()) {
            return;
        }

        Path readmePath = Paths.get(readmePathStr).normalize().toAbsolutePath();
        File file = readmePath.toFile();
        if (!file.exists()) {
            log.warn("README.md not found at {}, skipping README update", readmePath);
            return;
        }

        try {
            String content = Files.readString(readmePath);
            int startIdx = content.indexOf(SUMMARY_START_TAG);
            int endIdx = content.indexOf(SUMMARY_END_TAG);

            if (startIdx == -1 || endIdx == -1 || startIdx >= endIdx) {
                log.warn("Could not find dynamic tags in README.md. Looking for {} and {}", SUMMARY_START_TAG, SUMMARY_END_TAG);
                return;
            }

            String summarySection = buildSummaryMarkdown(stats, topCves);
            String updatedContent = content.substring(0, startIdx + SUMMARY_START_TAG.length())
                    + "\n" + summarySection + "\n"
                    + content.substring(endIdx);

            Files.writeString(readmePath, updatedContent);
            log.info("Successfully updated dynamic section in README.md");
        } catch (IOException e) {
            log.error("Failed to update README.md: {}", e.getMessage(), e);
        }
    }

    String buildSummaryMarkdown(CveStats stats, List<EnrichedCve> cves) {
        StringBuilder sb = new StringBuilder();
        sb.append("### État de la veille (Stack Spring Boot & Angular)\n\n");

        String timestamp = (stats != null && stats.lastSyncTimestamp() != null) ? stats.lastSyncTimestamp() : "N/A";
        int total = stats != null ? stats.totalTracked() : 0;
        int kev = stats != null ? stats.kevCount() : 0;
        int critical = stats != null ? stats.criticalCount() : 0;
        int high = stats != null ? stats.highCount() : 0;

        sb.append(String.format("> **Dernière synchronisation** : `%s`  \n", timestamp));
        sb.append(String.format("> **Vulnérabilités suivies** : `%d` | **Exploits CISA KEV** : `%d` | **Critiques** : `%d` | **Élevées** : `%d`\n\n",
                total, kev, critical, high));

        sb.append("| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |\n");
        sb.append("|---|---|---|---|---|---|---|\n");

        List<EnrichedCve> displayList = cves.stream()
                .filter(c -> "CRITICAL".equalsIgnoreCase(c.severity()) || "HIGH".equalsIgnoreCase(c.severity()) || c.inKev())
                .limit(8)
                .toList();

        if (displayList.isEmpty()) {
            sb.append("| - | - | - | - | - | - | Aucune vulnérabilité critique ou élevée récente. |\n");
        } else {
            for (EnrichedCve cve : displayList) {
                String cvss = cve.cvssScore() != null ? String.format("%.1f", cve.cvssScore()) : "N/A";
                String epss = cve.epssScore() != null ? String.format("%.1f%%", cve.epssScore() * 100) : "N/A";
                String inKev = cve.inKev() ? "**OUI**" : "Non";
                String stack = (cve.matchedKeywords() != null && !cve.matchedKeywords().isEmpty())
                        ? "`" + String.join(", ", cve.matchedKeywords()) + "`"
                        : "Stack";
                String desc = truncate(escapePipe(cve.description()), 75);

                sb.append(String.format("| [%s](%s) | `%s` | %s | %s | %s | %s | %s |\n",
                        cve.id(), cve.nvdUrl(), cve.severity(), cvss, epss, inKev, stack, desc));
            }
        }

        sb.append("\n*Données détaillées historisées chaque jour dans [`data/`](data/) • Alimenté par NVD 2.0, CISA KEV & EPSS*");
        return sb.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private String escapePipe(String text) {
        if (text == null) return "";
        return text.replace("|", "\\|").replace("\n", " ").replace("\r", "");
    }
}
