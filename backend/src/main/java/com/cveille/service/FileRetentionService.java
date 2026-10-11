package com.cveille.service;

import com.cveille.config.CveilleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service responsible solely for managing the retention policy on archived daily CVE files.
 */
@Service
public class FileRetentionService {

    private static final Logger log = LoggerFactory.getLogger(FileRetentionService.class);

    private final CveilleProperties properties;

    public FileRetentionService(CveilleProperties properties) {
        this.properties = properties;
    }

    /**
     * Retains only the most recent daily JSON files according to the configured retention limit.
     */
    public void enforceRetention(Path dataDir) {
        int maxFiles = (properties.retention() != null) ? properties.retention().maxDailyFiles() : 15;
        if (maxFiles <= 0 || !Files.exists(dataDir)) {
            return;
        }

        try (var stream = Files.list(dataDir)) {
            List<Path> dailyFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2}\\.json"))
                    .sorted((p1, p2) -> p2.getFileName().toString().compareTo(p1.getFileName().toString()))
                    .toList();

            if (dailyFiles.size() > maxFiles) {
                List<Path> filesToDelete = dailyFiles.subList(maxFiles, dailyFiles.size());
                for (Path p : filesToDelete) {
                    Files.deleteIfExists(p);
                    log.info("Purged old daily CVE data file: {}", p.getFileName());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to purge old daily CVE files: {}", e.getMessage());
        }
    }

    public void enforceRetention() {
        Path dataDir = Paths.get(properties.dataDir()).normalize().toAbsolutePath();
        enforceRetention(dataDir);
    }
}
