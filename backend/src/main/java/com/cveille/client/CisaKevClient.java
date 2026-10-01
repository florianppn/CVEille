package com.cveille.client;

import com.cveille.config.CveilleProperties;
import com.cveille.model.KevCatalog;
import com.cveille.model.KevItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CisaKevClient {

    private static final Logger log = LoggerFactory.getLogger(CisaKevClient.class);

    private final RestClient restClient;
    private final CveilleProperties properties;
    private final Map<String, KevItem> kevCache = new ConcurrentHashMap<>();

    public CisaKevClient(RestClient restClient, CveilleProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Loads or reloads the CISA KEV catalog into memory for O(1) lookups.
     */
    public synchronized Map<String, KevItem> loadCatalog() {
        if (!kevCache.isEmpty()) {
            return Collections.unmodifiableMap(kevCache);
        }

        String url = properties.cisaKev().feedUrl();
        log.info("Fetching CISA KEV catalog from {}", url);

        try {
            KevCatalog catalog = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(KevCatalog.class);

            if (catalog != null && catalog.vulnerabilities() != null) {
                for (KevItem item : catalog.vulnerabilities()) {
                    if (item.cveID() != null) {
                        kevCache.put(item.cveID().trim().toUpperCase(), item);
                    }
                }
                log.info("Successfully loaded {} CISA KEV entries", kevCache.size());
            } else {
                log.warn("CISA KEV catalog was empty or null");
            }
        } catch (Exception e) {
            log.error("Failed to load CISA KEV catalog: {}", e.getMessage(), e);
        }

        return Collections.unmodifiableMap(kevCache);
    }

    /**
     * Check if a CVE ID is listed in the CISA KEV catalog.
     */
    public Optional<KevItem> findKev(String cveId) {
        if (cveId == null) return Optional.empty();
        if (kevCache.isEmpty()) {
            loadCatalog();
        }
        return Optional.ofNullable(kevCache.get(cveId.trim().toUpperCase()));
    }

    public int getCachedCount() {
        return kevCache.size();
    }
}
