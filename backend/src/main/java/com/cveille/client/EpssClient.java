package com.cveille.client;

import com.cveille.config.CveilleProperties;
import com.cveille.model.EpssItem;
import com.cveille.model.EpssResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class EpssClient {

    private static final Logger log = LoggerFactory.getLogger(EpssClient.class);

    private final RestClient restClient;
    private final CveilleProperties properties;

    public EpssClient(RestClient restClient, CveilleProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Fetch EPSS scores for a list of CVE IDs in batched HTTP GET requests.
     */
    public Map<String, EpssItem> fetchEpssScores(Collection<String> cveIds) {
        if (cveIds == null || cveIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, EpssItem> results = new HashMap<>();
        List<String> idList = new ArrayList<>(cveIds);
        int batchSize = properties.epss().batchSize() > 0 ? properties.epss().batchSize() : 50;

        for (int i = 0; i < idList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, idList.size());
            List<String> batch = idList.subList(i, end);
            String commaJoined = String.join(",", batch);

            String url = properties.epss().apiUrl() + "?cve=" + commaJoined;
            try {
                EpssResponse response = restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(EpssResponse.class);

                if (response != null && response.data() != null) {
                    for (EpssItem item : response.data()) {
                        if (item.cve() != null) {
                            results.put(item.cve().toUpperCase(), item);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch EPSS batch for {} items: {}", batch.size(), e.getMessage());
            }
        }

        log.info("Fetched EPSS scores for {} / {} requested CVEs", results.size(), cveIds.size());
        return results;
    }
}
