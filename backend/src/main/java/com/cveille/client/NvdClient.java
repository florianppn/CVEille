package com.cveille.client;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveItem;
import com.cveille.model.NvdResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class NvdClient {

    private static final Logger log = LoggerFactory.getLogger(NvdClient.class);
    private static final DateTimeFormatter NVD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000").withZone(ZoneOffset.UTC);

    private final RestClient restClient;
    private final CveilleProperties properties;

    public NvdClient(RestClient restClient, CveilleProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Fetches all CVEs published within the sliding window (e.g. last 48 hours).
     */
    public List<CveItem> fetchRecentCves(int windowHours) {
        Instant now = Instant.now();
        Instant start = now.minus(windowHours, ChronoUnit.HOURS);

        String pubStartDate = NVD_DATE_FORMAT.format(start);
        String pubEndDate = NVD_DATE_FORMAT.format(now);

        log.info("Querying NVD API 2.0 for window [{} to {}] ({} hours)", pubStartDate, pubEndDate, windowHours);

        List<CveItem> allCves = new ArrayList<>();
        int startIndex = 0;
        int pageSize = properties.nvd().pageSize() > 0 ? properties.nvd().pageSize() : 200;
        int totalResults = Integer.MAX_VALUE;

        while (startIndex < totalResults) {
            URI uri = UriComponentsBuilder.fromUriString(properties.nvd().apiUrl())
                    .queryParam("pubStartDate", pubStartDate)
                    .queryParam("pubEndDate", pubEndDate)
                    .queryParam("resultsPerPage", pageSize)
                    .queryParam("startIndex", startIndex)
                    .build()
                    .toUri();

            NvdResponse page = executeWithRetry(uri);
            if (page == null) {
                log.warn("NVD page at index {} returned null or failed all retries.", startIndex);
                break;
            }

            totalResults = page.totalResults() != null ? page.totalResults() : 0;
            if (page.vulnerabilities() != null && !page.vulnerabilities().isEmpty()) {
                for (NvdResponse.DefCveItem defItem : page.vulnerabilities()) {
                    if (defItem.cve() != null) {
                        allCves.add(defItem.cve());
                    }
                }
                log.info("Fetched {} / {} CVEs from NVD (page: {} items)", allCves.size(), totalResults, page.vulnerabilities().size());
                startIndex += page.vulnerabilities().size();
            } else {
                break;
            }

            // Respect NIST NVD rate limit: sleep between requests if more pages exist
            if (startIndex < totalResults) {
                long sleepTime = (properties.nvd().apiKey() != null && !properties.nvd().apiKey().isBlank()) ? 800L : 6000L;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Total CVEs fetched from NVD: {}", allCves.size());
        return allCves;
    }

    private NvdResponse executeWithRetry(URI uri) {
        int maxRetries = properties.nvd().maxRetries() > 0 ? properties.nvd().maxRetries() : 3;
        long delay = properties.nvd().retryDelayMs() > 0 ? properties.nvd().retryDelayMs() : 3000L;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            final int currentAttempt = attempt;
            try {
                RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(uri);

                if (properties.nvd().apiKey() != null && !properties.nvd().apiKey().isBlank()) {
                    spec.header("apiKey", properties.nvd().apiKey());
                }

                return spec.retrieve()
                        .onStatus(HttpStatusCode::isError, (req, resp) -> {
                            log.warn("NVD API returned HTTP {} on attempt {}", resp.getStatusCode(), currentAttempt);
                        })
                        .body(NvdResponse.class);
            } catch (Exception e) {
                log.warn("NVD attempt {}/{} failed for URI {}: {}", attempt, maxRetries, uri, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
