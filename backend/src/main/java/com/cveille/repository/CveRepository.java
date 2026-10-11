package com.cveille.repository;

import com.cveille.model.CveStats;
import com.cveille.model.EnrichedCve;
import com.cveille.model.SyncSummary;

import java.util.List;
import java.util.Map;

/**
 * Persistence abstraction for CVE intelligence datasets.
 * Decouples storage details from business services and presentation controllers.
 */
public interface CveRepository {

    /**
     * Retrieves all tracked CVEs from the aggregated index.
     */
    List<EnrichedCve> findAll();

    /**
     * Retrieves CVEs matching the specified query filters.
     */
    List<EnrichedCve> findWithFilters(String search, String severity, Boolean kevOnly, String tech);

    /**
     * Reads all existing CVEs into a map indexed by CVE ID.
     */
    Map<String, EnrichedCve> findAllAsMap();

    /**
     * Saves the aggregated index to storage and mirrors it to the frontend.
     */
    void saveIndex(List<EnrichedCve> cves, CveStats stats, int newToday, String lastSync);

    /**
     * Checks if a daily archive file already exists for the given date.
     */
    boolean existsDaily(String dateStr);

    /**
     * Saves the daily sync summary archive file.
     */
    void saveDaily(String dateStr, SyncSummary summary);
}
