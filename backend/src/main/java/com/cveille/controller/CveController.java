package com.cveille.controller;

import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;
import com.cveille.repository.CveRepository;
import com.cveille.service.CveSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST presentation layer for CVE intelligence queries and manual sync triggers.
 * Strictly adheres to MVC: delegates data queries to CveRepository and execution to CveSyncService.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CveController {

    private final CveRepository cveRepository;
    private final CveSyncService cveSyncService;

    public CveController(CveRepository cveRepository, CveSyncService cveSyncService) {
        this.cveRepository = cveRepository;
        this.cveSyncService = cveSyncService;
    }

    @GetMapping("/cves")
    public ResponseEntity<List<EnrichedCve>> getCves(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean kevOnly,
            @RequestParam(required = false) String tech
    ) {
        List<EnrichedCve> results = cveRepository.findWithFilters(search, severity, kevOnly, tech);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncSummary> triggerSync() {
        SyncSummary summary = cveSyncService.runSync();
        return ResponseEntity.ok(summary);
    }
}
