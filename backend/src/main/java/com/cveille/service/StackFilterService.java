package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class StackFilterService {

    private final CveilleProperties properties;
    private final Map<String, Pattern> keywordPatterns = new HashMap<>();

    public StackFilterService(CveilleProperties properties) {
        this.properties = properties;
        initPatterns();
    }

    private void initPatterns() {
        if (properties.stack() != null && properties.stack().keywords() != null) {
            for (String kw : properties.stack().keywords()) {
                String cleanKw = kw.trim().toLowerCase();
                // Match keyword as whole word or hyphenated identifier
                String regex = "(?i)(^|[^a-zA-Z0-9_-])" + Pattern.quote(cleanKw) + "([^a-zA-Z0-9_-]|$)";
                keywordPatterns.put(cleanKw, Pattern.compile(regex));
            }
        }
    }

    /**
     * Finds all configured stack keywords matching the given CVE item.
     * Searches across description, CPE criteria, and CVE identifiers.
     */
    public List<String> findMatchedKeywords(CveItem cve) {
        if (cve == null) return Collections.emptyList();

        StringBuilder searchableText = new StringBuilder();
        if (cve.id() != null) searchableText.append(cve.id()).append(" ");
        if (cve.getEnglishDescription() != null) searchableText.append(cve.getEnglishDescription()).append(" ");

        if (cve.configurations() != null) {
            for (CveItem.Configuration config : cve.configurations()) {
                if (config.nodes() != null) {
                    for (CveItem.Node node : config.nodes()) {
                        if (node.cpeMatch() != null) {
                            for (CveItem.CpeMatch cpe : node.cpeMatch()) {
                                if (cpe.criteria() != null) {
                                    searchableText.append(cpe.criteria()).append(" ");
                                }
                            }
                        }
                    }
                }
            }
        }

        String targetText = searchableText.toString().toLowerCase();
        Set<String> matched = new LinkedHashSet<>();

        for (Map.Entry<String, Pattern> entry : keywordPatterns.entrySet()) {
            String kw = entry.getKey();
            Pattern pattern = entry.getValue();

            // Direct regex word boundary match
            if (pattern.matcher(targetText).find()) {
                matched.add(kw);
            } else if (targetText.contains(":" + kw + ":") || targetText.contains("/" + kw + "/")) {
                // Common CPE or URL pattern format
                matched.add(kw);
            }
        }

        return new ArrayList<>(matched);
    }

    public boolean matchesStack(CveItem cve) {
        return !findMatchedKeywords(cve).isEmpty();
    }
}
