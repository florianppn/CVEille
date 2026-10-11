package com.cveille.repository;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class FileCveRepository implements CveRepository {

    private static final Logger log = LoggerFactory.getLogger(FileCveRepository.class);

    private final CveilleProperties properties;
    private final ObjectMapper objectMapper;

    public FileCveRepository(CveilleProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EnrichedCve> findAll() {
        return new ArrayList<>(findAllAsMap().values());
    }

    @Override
    public List<EnrichedCve> findWithFilters(String search, String severity, Boolean kevOnly, String tech) {
        List<EnrichedCve> all = findAll();
        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        return all.stream()
                .filter(cve -> {
                    if (kevOnly != null && kevOnly && !cve.inKev()) return false;
                    if (severity != null && !severity.isBlank() && !severity.equalsIgnoreCase(cve.severity())) return false;
                    if (tech != null && !tech.isBlank()) {
                        if (cve.matchedKeywords() == null || !cve.matchedKeywords().contains(tech.toLowerCase())) return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String q = search.toLowerCase();
                        boolean matchId = cve.id() != null && cve.id().toLowerCase().contains(q);
                        boolean matchDesc = cve.description() != null && cve.description().toLowerCase().contains(q);
                        boolean matchKeywords = cve.matchedKeywords() != null && cve.matchedKeywords().stream().anyMatch(k -> k.toLowerCase().contains(q));
                        return matchId || matchDesc || matchKeywords;
                    }
                    return true;
                })
                .toList();
    }

    @Override
    public Map<String, EnrichedCve> findAllAsMap() {
        Path indexPath = getIndexPath();
        if (!Files.exists(indexPath)) {
            return new LinkedHashMap<>();
        }

        try {
            Map<String, Object> indexMap = objectMapper.readValue(indexPath.toFile(), new TypeReference<>() {});
            if (!indexMap.containsKey("cves")) {
                return new LinkedHashMap<>();
            }

            List<Map<String, Object>> rawList = (List<Map<String, Object>>) indexMap.get("cves");
            Map<String, EnrichedCve> map = new LinkedHashMap<>();
            if (rawList != null) {
                for (Map<String, Object> raw : rawList) {
                    EnrichedCve cve = objectMapper.convertValue(raw, EnrichedCve.class);
                    if (cve != null && cve.id() != null) {
                        map.put(cve.id(), cve);
                    }
                }
            }
            return map;
        } catch (IOException e) {
            log.warn("Could not read index.json at {}: {}", indexPath, e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    @Override
    public void saveIndex(List<EnrichedCve> cves, CveStats stats, int newToday, String lastSync) {
        try {
            Path dataDir = getDataDir();
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            Map<String, Object> indexPayload = new LinkedHashMap<>();
            indexPayload.put("lastSync", lastSync);
            indexPayload.put("newToday", newToday);
            indexPayload.put("stats", stats);
            indexPayload.put("cves", cves);

            Path indexFile = dataDir.resolve("index.json");
            objectMapper.writeValue(indexFile.toFile(), indexPayload);
            log.info("Saved aggregated index to {} ({} CVEs)", indexFile, cves.size());

            // Mirror to frontend data directory
            if (properties.frontendDir() != null && !properties.frontendDir().isBlank()) {
                Path frontendDataDir = Paths.get(properties.frontendDir(), "data").normalize().toAbsolutePath();
                if (!Files.exists(frontendDataDir)) {
                    Files.createDirectories(frontendDataDir);
                }
                Path frontendIndexFile = frontendDataDir.resolve("index.json");
                objectMapper.writeValue(frontendIndexFile.toFile(), indexPayload);
                log.info("Mirrored index.json to frontend at {}", frontendIndexFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save index.json: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsDaily(String dateStr) {
        Path dailyFile = getDataDir().resolve(dateStr + ".json");
        return Files.exists(dailyFile);
    }

    @Override
    public void saveDaily(String dateStr, SyncSummary summary) {
        try {
            Path dataDir = getDataDir();
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            Path dailyFile = dataDir.resolve(dateStr + ".json");
            objectMapper.writeValue(dailyFile.toFile(), summary);
            log.info("Saved daily archive file to {}", dailyFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save daily archive " + dateStr + ": " + e.getMessage(), e);
        }
    }

    private Path getDataDir() {
        return Paths.get(properties.dataDir()).normalize().toAbsolutePath();
    }

    private Path getIndexPath() {
        return getDataDir().resolve("index.json");
    }
}
