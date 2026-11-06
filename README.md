# CVEille

Veille quotidienne automatisée sur les vulnérabilités CVE ciblant les technologies Spring Boot et Angular. Les données proviennent du NIST NVD 2.0, du catalogue CISA KEV et des scores FIRST EPSS.

**Dashboard en ligne** : [https://florianppn.github.io/CVEille/](https://florianppn.github.io/CVEille/)

---

<!-- CVEILLE_SUMMARY_START -->
### Top 8 des menaces les plus dangereuses et prioritaires

> **Dernière synchronisation** : `2026-11-06T13:30:26.143834427Z`

| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |
|---|---|---|---|---|---|---|
| [CVE-2026-120338](https://nvd.nist.gov/vuln/detail/CVE-2026-120338) | `CRITICAL` | 10.0 | 0.8% | Non | `javascript` | Prefab UI's Generative UI sandbox runs caller-supplied Python inside Pyodid... |
| [CVE-2026-115188](https://nvd.nist.gov/vuln/detail/CVE-2026-115188) | `CRITICAL` | 9.9 | 0.7% | Non | `docker` | An unrestricted file upload vulnerability exists in the campaign import fea... |
| [CVE-2026-102649](https://nvd.nist.gov/vuln/detail/CVE-2026-102649) | `CRITICAL` | 9.9 | 0.5% | Non | `kubernetes` | Burrito is a Terraform Automation Collaboration Software (TACoS) Kubernetes... |
| [CVE-2026-120306](https://nvd.nist.gov/vuln/detail/CVE-2026-120306) | `CRITICAL` | 9.9 | N/A | Non | `kubernetes` | Beta9 turns each shell command in a tenant image definition into a Dockerfi... |
| [CVE-2026-120307](https://nvd.nist.gov/vuln/detail/CVE-2026-120307) | `CRITICAL` | 9.9 | N/A | Non | `kubernetes` | The Beta9 worker serves the sandbox file API from outside the container. ge... |
| [CVE-2026-97693](https://nvd.nist.gov/vuln/detail/CVE-2026-97693) | `CRITICAL` | 9.9 | 0.8% | Non | `docker, postgresql` | Yuxi is a large-model-based intelligent knowledge base and knowledge graph ... |
| [CVE-2026-122310](https://nvd.nist.gov/vuln/detail/CVE-2026-122310) | `CRITICAL` | 9.9 | 0.4% | Non | `kubernetes` | Kyverno from 1.19.1 before 1.19.2 validates a Policy's context.apiCall urlP... |
| [CVE-2026-46652](https://nvd.nist.gov/vuln/detail/CVE-2026-46652) | `CRITICAL` | 9.8 | 1.1% | Non | `tomcat` | The Goobi viewer is a web application that allows digitised material to be ... |

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
