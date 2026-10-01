package com.cveille.controller;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;
import com.cveille.service.CveSyncService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CveController {

    private final CveSyncService cveSyncService;
    private final CveilleProperties properties;
    private final ObjectMapper objectMapper;

    public CveController(CveSyncService cveSyncService, CveilleProperties properties, ObjectMapper objectMapper) {
        this.cveSyncService = cveSyncService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/cves")
    public ResponseEntity<List<EnrichedCve>> getCves(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean kevOnly,
            @RequestParam(required = false) String tech
    ) {
        Path indexPath = Paths.get(properties.dataDir()).resolve("index.json").normalize().toAbsolutePath();
        if (!indexPath.toFile().exists()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        try {
            Map<String, Object> indexMap = objectMapper.readValue(indexPath.toFile(), new TypeReference<>() {});
            List<Map<String, Object>> rawList = (List<Map<String, Object>>) indexMap.get("cves");
            if (rawList == null) return ResponseEntity.ok(Collections.emptyList());

            List<EnrichedCve> all = rawList.stream()
                    .map(m -> objectMapper.convertValue(m, EnrichedCve.class))
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
                            return matchId || matchDesc;
                        }
                        return true;
                    })
                    .toList();

            return ResponseEntity.ok(all);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        Path indexPath = Paths.get(properties.dataDir()).resolve("index.json").normalize().toAbsolutePath();
        if (!indexPath.toFile().exists()) {
            return ResponseEntity.ok(Map.of("message", "No data synced yet. Run POST /api/sync or --sync"));
        }

        try {
            Map<String, Object> indexMap = objectMapper.readValue(indexPath.toFile(), new TypeReference<>() {});
            return ResponseEntity.ok(indexMap.get("stats"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncSummary> triggerSync() {
        SyncSummary summary = cveSyncService.runSync();
        return ResponseEntity.ok(summary);
    }
}
