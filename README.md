# CVEille

Veille quotidienne automatisée sur les vulnérabilités CVE ciblant les technologies Spring Boot et Angular. Les données proviennent du NIST NVD 2.0, du catalogue CISA KEV et des scores FIRST EPSS.

**Dashboard en ligne** : [https://florianppn.github.io/CVEille/](https://florianppn.github.io/CVEille/)

---

<!-- CVEILLE_SUMMARY_START -->
### Top 8 des menaces les plus dangereuses et prioritaires

> **Dernière synchronisation** : `2026-10-28T11:53:53.444627916Z`

| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |
|---|---|---|---|---|---|---|
| [CVE-2026-53988](https://nvd.nist.gov/vuln/detail/CVE-2026-53988) | `CRITICAL` | 10.0 | N/A | Non | `docker` | Dockhand before 1.0.40 contains an authentication bypass vulnerability in i... |
| [CVE-2026-65162](https://nvd.nist.gov/vuln/detail/CVE-2026-65162) | `CRITICAL` | 10.0 | 0.7% | Non | `kubernetes` | NVIDIA AIStore for Linux contains a vulnerability in the legacy ETL Kuberne... |
| [CVE-2026-83938](https://nvd.nist.gov/vuln/detail/CVE-2026-83938) | `CRITICAL` | 10.0 | 0.7% | Non | `kubernetes` | Missing authentication for critical function in Azure Kubernetes Configurat... |
| [CVE-2026-77459](https://nvd.nist.gov/vuln/detail/CVE-2026-77459) | `CRITICAL` | 9.9 | 0.7% | Non | `kubernetes` | Argo CD is a declarative, GitOps continuous delivery tool for Kubernetes. F... |
| [CVE-2026-77524](https://nvd.nist.gov/vuln/detail/CVE-2026-77524) | `CRITICAL` | 9.9 | 0.6% | Non | `kubernetes` | KEDA is a Kubernetes-based Event Driven Autoscaling component. In version 2... |
| [CVE-2026-105037](https://nvd.nist.gov/vuln/detail/CVE-2026-105037) | `CRITICAL` | 9.9 | 0.4% | Non | `docker` | EnergizeLab station-middleware (NestJS) Docker images 1.6.5 through 1.8.2 a... |
| [CVE-2026-105312](https://nvd.nist.gov/vuln/detail/CVE-2026-105312) | `CRITICAL` | 9.9 | 0.3% | Non | `kubernetes` | A Kubernetes tenant with create permission on Console custom resources can ... |
| [CVE-2026-113672](https://nvd.nist.gov/vuln/detail/CVE-2026-113672) | `CRITICAL` | 9.9 | 0.8% | Non | `kubernetes` | Argo CD is a declarative, GitOps continuous delivery tool for Kubernetes. F... |

*Données détaillées historisées chaque jour dans [`data/`](data/) • Alimenté par NVD 2.0, CISA KEV & EPSS*
<!-- CVEILLE_SUMMARY_END -->

---

## Structure

```
├── backend/    # Service Spring Boot (ingestion NVD/KEV/EPSS, analyse, export JSON)
├── frontend/   # Dashboard statique (HTML, CSS, JS) hébergé sur GitHub Pages
└── data/       # Historique quotidien des vulnérabilités au format JSON (15 fichiers max)
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
