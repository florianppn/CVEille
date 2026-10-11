package com.cveille.service;

import com.cveille.config.CveilleProperties;
import com.cveille.model.CveItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StackFilterServiceTest {

    private StackFilterService stackFilterService;

    @BeforeEach
    void setUp() {
        CveilleProperties.Stack stack = new CveilleProperties.Stack(
                List.of("java", "javascript", "typescript", "spring", "spring-boot", "spring-security", "tomcat", "jackson", "angular", "docker")
        );
        CveilleProperties properties = new CveilleProperties(
                48, "../data", "../frontend", "../README.md", new CveilleProperties.Retention(15), stack, null, null, null, null
        );
        stackFilterService = new StackFilterService(properties);
    }

    @Test
    void shouldMatchSpringFrameworkVulnerability() {
        CveItem.Description desc = new CveItem.Description("en", "Remote code execution in Spring Framework through data binding");
        CveItem cve = new CveItem("CVE-2022-22965", "mitre", "2022-03-31", "2022-04-01", "Analyzed",
                List.of(desc), null, null);

        List<String> matched = stackFilterService.findMatchedKeywords(cve);

        assertThat(matched).contains("spring");
        assertThat(stackFilterService.matchesStack(cve)).isTrue();
    }

    @Test
    void shouldMatchJavaRuntimeVulnerability() {
        CveItem.Description desc = new CveItem.Description("en", "Vulnerability in Oracle Java SE, OpenJDK Java runtime environment allows sandbox escape");
        CveItem cve = new CveItem("CVE-2024-20918", "oracle", "2024-01-16", "2024-01-17", "Analyzed",
                List.of(desc), null, null);

        List<String> matched = stackFilterService.findMatchedKeywords(cve);

        assertThat(matched).contains("java");
        assertThat(stackFilterService.matchesStack(cve)).isTrue();
    }

    @Test
    void shouldMatchJavaScriptAndTypeScriptVulnerabilities() {
        CveItem.Description descJs = new CveItem.Description("en", "Prototype pollution in JavaScript core utility");
        CveItem cveJs = new CveItem("CVE-2024-1234", "mitre", "2024-02-01", "2024-02-02", "Analyzed",
                List.of(descJs), null, null);

        List<String> matchedJs = stackFilterService.findMatchedKeywords(cveJs);
        assertThat(matchedJs).contains("javascript");

        CveItem.Description descTs = new CveItem.Description("en", "Code execution in TypeScript compiler language service");
        CveItem cveTs = new CveItem("CVE-2024-5678", "mitre", "2024-02-03", "2024-02-04", "Analyzed",
                List.of(descTs), null, null);

        List<String> matchedTs = stackFilterService.findMatchedKeywords(cveTs);
        assertThat(matchedTs).contains("typescript");
    }

    @Test
    void shouldMatchAngularVulnerability() {
        CveItem.Description desc = new CveItem.Description("en", "Cross-site scripting (XSS) vulnerability discovered in Angular router");
        CveItem cve = new CveItem("CVE-2024-9999", "mitre", "2024-01-15", "2024-01-16", "Analyzed",
                List.of(desc), null, null);

        List<String> matched = stackFilterService.findMatchedKeywords(cve);

        assertThat(matched).contains("angular");
        assertThat(stackFilterService.matchesStack(cve)).isTrue();
    }

    @Test
    void shouldMatchTomcatCpeConfiguration() {
        CveItem.Description desc = new CveItem.Description("en", "Information disclosure issue");
        CveItem.CpeMatch cpe = new CveItem.CpeMatch(true, "cpe:2.3:a:apache:tomcat:10.1.0:*:*:*:*:*:*:*", "uuid-123");
        CveItem.Node node = new CveItem.Node("OR", false, List.of(cpe));
        CveItem.Configuration config = new CveItem.Configuration(List.of(node));

        CveItem cve = new CveItem("CVE-2023-1111", "mitre", "2023-05-10", "2023-05-11", "Analyzed",
                List.of(desc), null, List.of(config));

        List<String> matched = stackFilterService.findMatchedKeywords(cve);

        assertThat(matched).contains("tomcat");
    }

    @Test
    void shouldNotMatchUnrelatedVulnerability() {
        CveItem.Description desc = new CveItem.Description("en", "SQL injection in obscure-cms plugin for Joomla");
        CveItem cve = new CveItem("CVE-2023-0000", "mitre", "2023-01-01", "2023-01-02", "Analyzed",
                List.of(desc), null, null);

        List<String> matched = stackFilterService.findMatchedKeywords(cve);

        assertThat(matched).isEmpty();
        assertThat(stackFilterService.matchesStack(cve)).isFalse();
    }
}
