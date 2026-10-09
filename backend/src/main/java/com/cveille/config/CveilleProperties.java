package com.cveille.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "cveille")
public record CveilleProperties(
        int windowHours,
        String dataDir,
        String frontendDir,
        String readmePath,
        Retention retention,
        Stack stack,
        Alert alert,
        Nvd nvd,
        CisaKev cisaKev,
        Epss epss,
        Notifications notifications
) {
    public CveilleProperties(
            int windowHours,
            String dataDir,
            String frontendDir,
            String readmePath,
            Stack stack,
            Alert alert,
            Nvd nvd,
            CisaKev cisaKev,
            Epss epss,
            Notifications notifications
    ) {
        this(windowHours, dataDir, frontendDir, readmePath, new Retention(15), stack, alert, nvd, cisaKev, epss, notifications);
    }

    public record Retention(int maxDailyFiles) {
        public Retention {
            if (maxDailyFiles <= 0) {
                maxDailyFiles = 15;
            }
        }
    }

    public record Stack(List<String> keywords) {}

    public record Alert(
            double minCvss,
            boolean alertOnKev,
            double minEpss
    ) {}

    public record Nvd(
            String apiUrl,
            String apiKey,
            int pageSize,
            int maxRetries,
            long retryDelayMs
    ) {}

    public record CisaKev(
            String feedUrl
    ) {}

    public record Epss(
            String apiUrl,
            int batchSize
    ) {}

    public record Notifications(
            Discord discord,
            Telegram telegram
    ) {
        public record Discord(boolean enabled, String webhookUrl) {}
        public record Telegram(boolean enabled, String botToken, String chatId) {}
    }
}
