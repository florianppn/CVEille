# CVEille

Veille quotidienne automatisée sur les vulnérabilités CVE ciblant les technologies Spring Boot et Angular. Les données proviennent du NIST NVD 2.0, du catalogue CISA KEV et des scores FIRST EPSS.

**Dashboard en ligne** : [https://florianppn.github.io/CVEille/](https://florianppn.github.io/CVEille/)

---

<!-- CVEILLE_SUMMARY_START -->
### Top 8 des menaces les plus dangereuses et prioritaires

> **Dernière synchronisation** : `2026-11-21T13:57:35.037174029Z`

| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |
|---|---|---|---|---|---|---|
| [CVE-2026-114824](https://nvd.nist.gov/vuln/detail/CVE-2026-114824) | `CRITICAL` | 10.0 | 0.4% | Non | `java` | Vulnerability in the Oracle Platform Security for Java product of Oracle Fu... |
| [CVE-2026-71499](https://nvd.nist.gov/vuln/detail/CVE-2026-71499) | `CRITICAL` | 10.0 | 0.4% | Non | `javascript` | cf-manager is an all-in-one dashboard for managing multiple Cloudflare acco... |
| [CVE-2026-17146](https://nvd.nist.gov/vuln/detail/CVE-2026-17146) | `CRITICAL` | 9.9 | 0.7% | Non | `javascript` | IBM API Connect 10.0.8.0 through 10.0.8.12 and 12.1.0.0 through 12.1.1.2 co... |
| [CVE-2026-123800](https://nvd.nist.gov/vuln/detail/CVE-2026-123800) | `CRITICAL` | 9.9 | 0.6% | Non | `java` | SuperTokens Core is an open source authentication server. From 6.0.0 until ... |
| [CVE-2026-127073](https://nvd.nist.gov/vuln/detail/CVE-2026-127073) | `CRITICAL` | 9.9 | 0.5% | Non | `kubernetes` | OpsTree BuildPiper before 2.7.0 contains a privilege escalation vulnerabili... |
| [CVE-2026-127080](https://nvd.nist.gov/vuln/detail/CVE-2026-127080) | `CRITICAL` | 9.9 | 0.5% | Non | `kubernetes` | OpsTree BuildPiper before 2.7.0 contains an improper authorization vulnerab... |
| [CVE-2026-129433](https://nvd.nist.gov/vuln/detail/CVE-2026-129433) | `CRITICAL` | 9.9 | N/A | Non | `docker` | Dokploy before 0.31.1 contains an OS command injection vulnerability in get... |
| [CVE-2026-129987](https://nvd.nist.gov/vuln/detail/CVE-2026-129987) | `CRITICAL` | 9.9 | 1.1% | Non | `postgresql` | Dokploy is a free, self-hostable Platform as a Service (PaaS). Prior to 0.3... |

*Données détaillées historisées chaque jour dans [`data/`](data/) • Alimenté par NVD 2.0, CISA KEV & EPSS*
<!-- CVEILLE_SUMMARY_END -->

---

## Structure

```
├── backend/    # Service Spring Boot (ingestion NVD/KEV/EPSS, analyse, export JSON)
├── frontend/   # Dashboard statique (HTML, CSS, JS) hébergé sur GitHub Pages
└── data/       # Historique quotidien des vulnérabilités au format JSON (14 fichiers max / 2 semaines)
```

## Stack surveillée

- **Backend** : Java, Spring, Spring Boot, Spring Security, Tomcat, Hibernate, Jackson, Log4j, Logback
- **Frontend** : JavaScript, TypeScript, Angular, RxJS, Node.js, npm
- **Infrastructure** : Docker, Kubernetes, PostgreSQL, Nginx, Keycloak

*Configurable dans `backend/src/main/resources/application.yml`.*

## Lancement local

### Backend (Java 25 / Maven)

```bash
cd backend

# Lancer la synchronisation
mvn spring-boot:run -Dspring-boot.run.arguments="--sync"

# Exécuter les tests unitaires
mvn test
```

### Frontend

```bash
python3 -m http.server 3000 --directory frontend
# Ouvrir http://localhost:3000
```

## Licence

[MIT](LICENSE)
