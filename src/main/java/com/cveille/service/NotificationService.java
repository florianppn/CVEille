package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.EnrichedCve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RestClient restClient;
    private final CveilleProperties properties;

    public NotificationService(RestClient restClient, CveilleProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Sends alerts to configured channels (Discord webhook / Telegram) for high-priority CVEs.
     */
    public void sendAlerts(List<EnrichedCve> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            log.info("No critical CVE alerts to notify.");
            return;
        }

        if (properties.notifications() != null &&
                properties.notifications().discord() != null &&
                properties.notifications().discord().enabled()) {
            sendDiscordAlerts(alerts);
        }
    }

    private void sendDiscordAlerts(List<EnrichedCve> alerts) {
        String webhookUrl = properties.notifications().discord().webhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Discord webhook URL is not configured. Skipping Discord notification.");
            return;
        }

        log.info("Sending {} critical alerts to Discord webhook", alerts.size());

        // Send up to 5 embeds per message
        int batchSize = 5;
        for (int i = 0; i < alerts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, alerts.size());
            List<EnrichedCve> subList = alerts.subList(i, end);

            List<Map<String, Object>> embeds = new ArrayList<>();
            for (EnrichedCve cve : subList) {
                int color = cve.inKev() ? 0xFF0000 : (cve.cvssScore() != null && cve.cvssScore() >= 9.0 ? 0xE02424 : 0xF59E0B);

                Map<String, Object> embed = new LinkedHashMap<>();
                embed.put("title", "🚨 " + cve.id() + " - " + cve.severity());
                embed.put("url", cve.nvdUrl());
                embed.put("color", color);

                String description = cve.description() != null ? cve.description() : "Pas de description fournie.";
                if (description.length() > 300) {
                    description = description.substring(0, 297) + "...";
                }
                embed.put("description", description);

                List<Map<String, Object>> fields = new ArrayList<>();
                fields.add(Map.of("name", "CVSS Score", "value", cve.cvssScore() != null ? cve.cvssScore().toString() : "N/A", "inline", true));
                fields.add(Map.of("name", "CISA KEV (Exploité)", "value", cve.inKev() ? "🔥 OUI" : "Non", "inline", true));
                fields.add(Map.of("name", "EPSS (30j)", "value", cve.epssScore() != null ? String.format(Locale.US, "%.2f%%", cve.epssScore() * 100) : "N/A", "inline", true));
                fields.add(Map.of("name", "Stack ciblée", "value", cve.matchedKeywords() != null ? String.join(", ", cve.matchedKeywords()) : "Général", "inline", false));

                embed.put("fields", fields);
                embed.put("footer", Map.of("text", "CVEille • Veille automatisée Spring Boot & Angular"));
                embeds.add(embed);
            }

            Map<String, Object> payload = Map.of(
                    "username", "CVEille Bot",
                    "avatar_url", "https://raw.githubusercontent.com/florianppn/CVEille/main/docs/assets/shield.png",
                    "embeds", embeds
            );

            try {
                restClient.post()
                        .uri(webhookUrl)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                log.warn("Failed to deliver Discord webhook: {}", e.getMessage());
            }
        }
    }
}
