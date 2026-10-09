# CVEille

Veille quotidienne automatisée sur les vulnérabilités CVE ciblant les technologies Spring Boot et Angular. Les données proviennent du NIST NVD 2.0, du catalogue CISA KEV et des scores FIRST EPSS.

**Dashboard en ligne** : [https://florianppn.github.io/CVEille/](https://florianppn.github.io/CVEille/)

---

<!-- CVEILLE_SUMMARY_START -->
### État de la veille (Stack Spring Boot & Angular)

> **Dernière synchronisation** : `2026-10-09T12:20:51.238598021Z`  
> **Vulnérabilités suivies** : `40` | **Exploits CISA KEV** : `0` | **Critiques** : `3` | **Élevées** : `8`

| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |
|---|---|---|---|---|---|---|
| [CVE-2026-53988](https://nvd.nist.gov/vuln/detail/CVE-2026-53988) | `CRITICAL` | 10.0 | N/A | Non | `docker` | Dockhand before 1.0.40 contains an authentication bypass vulnerability in its... |
| [CVE-2026-55494](https://nvd.nist.gov/vuln/detail/CVE-2026-55494) | `CRITICAL` | 9.8 | 0.8% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |
| [CVE-2026-105641](https://nvd.nist.gov/vuln/detail/CVE-2026-105641) | `CRITICAL` | 9.8 | 0.5% | Non | `docker` | Plane is an open-source project management tool. Prior to 1.4.0, the deployme... |
| [CVE-2026-107699](https://nvd.nist.gov/vuln/detail/CVE-2026-107699) | `CRITICAL` | 9.8 | N/A | Non | `node.js` | ppt2png through 0.0.6 contains an OS command injection vulnerability that all... |
| [CVE-2026-107700](https://nvd.nist.gov/vuln/detail/CVE-2026-107700) | `CRITICAL` | 9.8 | N/A | Non | `node.js` | dot-access 0.0.3 through 1.0.0 contains a code injection vulnerability that a... |
| [CVE-2026-107703](https://nvd.nist.gov/vuln/detail/CVE-2026-107703) | `CRITICAL` | 9.8 | N/A | Non | `node.js` | @enmaso/node-convert through 1.0.0 contains an OS command injection vulnerabi... |
| [CVE-2026-55181](https://nvd.nist.gov/vuln/detail/CVE-2026-55181) | `CRITICAL` | 9.4 | 0.7% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |
| [CVE-2026-62308](https://nvd.nist.gov/vuln/detail/CVE-2026-62308) | `CRITICAL` | 9.1 | 0.4% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |

*Données détaillées historisées chaque jour dans [`data/`](data/) • Alimenté par NVD 2.0, CISA KEV & EPSS*
<!-- CVEILLE_SUMMARY_END -->

---

## Structure

```
├── backend/    # Service Spring Boot (ingestion NVD/KEV/EPSS, analyse, export JSON)
├── frontend/   # Dashboard statique (HTML, CSS, JS) hébergé sur GitHub Pages
└── data/       # Historique quotidien des vulnérabilités au format JSON
```

## Stack surveillée

- **Backend** : Spring, Spring Boot, Spring Security, Tomcat, Hibernate, Jackson, Log4j, Logback
- **Frontend** : Angular, TypeScript, RxJS, Node.js, npm
- **Infrastructure** : Docker, Kubernetes, PostgreSQL, Nginx, Keycloak

*Configurable dans `backend/src/main/resources/application.yml`.*

## Lancement local

### Backend (Java 21 / Maven)

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

## Variables d'environnement

- `NVD_API_KEY` *(optionnel)* : Clé d'API NIST NVD pour augmenter le quota de requêtes.
- `DISCORD_WEBHOOK_URL` *(optionnel)* : Webhook Discord pour recevoir des notifications sur les vulnérabilités critiques.

## Licence

[MIT](LICENSE)
