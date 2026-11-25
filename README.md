# CVEille

Veille quotidienne automatisée sur les vulnérabilités CVE ciblant les technologies Spring Boot et Angular. Les données proviennent du NIST NVD 2.0, du catalogue CISA KEV et des scores FIRST EPSS.

**Dashboard en ligne** : [https://florianppn.github.io/CVEille/](https://florianppn.github.io/CVEille/)

---

<!-- CVEILLE_SUMMARY_START -->
### Top 8 des menaces les plus dangereuses et prioritaires

> **Dernière synchronisation** : `2026-11-25T16:33:32.319451908Z`

| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |
|---|---|---|---|---|---|---|
| [CVE-2026-114824](https://nvd.nist.gov/vuln/detail/CVE-2026-114824) | `CRITICAL` | 10.0 | 0.4% | Non | `java` | Vulnerability in the Oracle Platform Security for Java product of Oracle Fu... |
| [CVE-2026-71499](https://nvd.nist.gov/vuln/detail/CVE-2026-71499) | `CRITICAL` | 10.0 | 0.4% | Non | `javascript` | cf-manager is an all-in-one dashboard for managing multiple Cloudflare acco... |
| [CVE-2026-127073](https://nvd.nist.gov/vuln/detail/CVE-2026-127073) | `CRITICAL` | 9.9 | 0.5% | Non | `kubernetes` | OpsTree BuildPiper before 2.7.0 contains a privilege escalation vulnerabili... |
| [CVE-2026-127080](https://nvd.nist.gov/vuln/detail/CVE-2026-127080) | `CRITICAL` | 9.9 | 0.5% | Non | `kubernetes` | OpsTree BuildPiper before 2.7.0 contains an improper authorization vulnerab... |
| [CVE-2026-129433](https://nvd.nist.gov/vuln/detail/CVE-2026-129433) | `CRITICAL` | 9.9 | N/A | Non | `docker` | Dokploy before 0.31.1 contains an OS command injection vulnerability in get... |
| [CVE-2026-129987](https://nvd.nist.gov/vuln/detail/CVE-2026-129987) | `CRITICAL` | 9.9 | 1.1% | Non | `postgresql` | Dokploy is a free, self-hostable Platform as a Service (PaaS). Prior to 0.3... |
| [CVE-2026-129316](https://nvd.nist.gov/vuln/detail/CVE-2026-129316) | `CRITICAL` | 9.9 | 0.7% | Non | `docker` | Dokploy is a free, self-hostable Platform as a Service (PaaS). From 0.29.13... |
| [CVE-2026-129989](https://nvd.nist.gov/vuln/detail/CVE-2026-129989) | `CRITICAL` | 9.9 | 0.9% | Non | `docker` | Dokploy is a free, self-hostable Platform as a Service (PaaS). Prior to 0.3... |

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
