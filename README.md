# 🛡️ CVEille

<p align="center">
  <a href="https://florianppn.github.io/CVEille/"><img src="https://img.shields.io/badge/Live_Dashboard-GitHub_Pages-0ea5e9?style=for-the-badge&logo=githubpages&logoColor=white" alt="Live Dashboard" /></a>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3" />
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Threat%20Intel-NVD%20%2B%20CISA%20KEV%20%2B%20EPSS-0052CC?style=for-the-badge" alt="Threat Intel" />
</p>

> **CVEille** est un outil de veille quotidienne automatisée sur les vulnérabilités de sécurité (CVE).  
> Propulsé par un pipeline **Spring Boot 3**, il filtre les failles ciblant l'écosystème **Spring Boot & Angular**, croise la criticité (**CVSS**), l'exploitation active (**CISA KEV**) et la prédiction de risque (**EPSS**), et publie chaque jour un dashboard web interactif.

🌐 **Dashboard interactif en ligne** : **[https://florianppn.github.io/CVEille/](https://florianppn.github.io/CVEille/)**

---

<!-- CVEILLE_SUMMARY_START -->
### 🛡️ État de la veille CVEille (Stack Spring Boot & Angular)

> **Dernière synchronisation** : `2026-10-02T11:39:13.564914848Z`  
> **Vulnérabilités suivies** : `14` | 🔥 **Exploits CISA KEV** : `0` | 🚨 **Critiques** : `3` | ⚠️ **Élevées** : `3`

| CVE ID | Sévérité | CVSS | EPSS | KEV | Stack | Description |
|---|---|---|---|---|---|---|
| [CVE-2026-53988](https://nvd.nist.gov/vuln/detail/CVE-2026-53988) | `CRITICAL` | 10.0 | N/A | Non | `docker` | Dockhand before 1.0.40 contains an authentication bypass vulnerability in its... |
| [CVE-2026-55494](https://nvd.nist.gov/vuln/detail/CVE-2026-55494) | `CRITICAL` | 9.8 | 0.8% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |
| [CVE-2026-55181](https://nvd.nist.gov/vuln/detail/CVE-2026-55181) | `CRITICAL` | 9.4 | 0.7% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |
| [CVE-2026-62308](https://nvd.nist.gov/vuln/detail/CVE-2026-62308) | `CRITICAL` | 9.1 | 0.4% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |
| [CVE-2026-102676](https://nvd.nist.gov/vuln/detail/CVE-2026-102676) | `HIGH` | 8.3 | N/A | Non | `node.js` | Electron is a framework for writing cross-platform desktop applications using... |
| [CVE-2026-102826](https://nvd.nist.gov/vuln/detail/CVE-2026-102826) | `HIGH` | 8.1 | N/A | Non | `node.js` | simple-git, an interface for running git commands in any node.js application,... |
| [CVE-2026-102827](https://nvd.nist.gov/vuln/detail/CVE-2026-102827) | `HIGH` | 8.1 | N/A | Non | `node.js` | simple-git, an interface for running git commands in any node.js application,... |
| [CVE-2026-87004](https://nvd.nist.gov/vuln/detail/CVE-2026-87004) | `HIGH` | 8.1 | 0.3% | Non | `docker` | Tugtainer is a self-hosted app for automating updates of Docker containers. P... |

*Données détaillées historisées chaque jour dans [`data/`](data/) • Alimenté par NVD 2.0, CISA KEV & EPSS*
<!-- CVEILLE_SUMMARY_END -->

---

## 🔬 Méthode de priorisation

Pour aller au-delà du simple score CVSS et mesurer le risque concret :

- **NVD API 2.0 (NIST)** : gravité intrinsèque (**CVSS v3.1**).
- **CISA KEV Catalog** : confirmation d'**exploitation active réelle** par des attaquants dans la nature.
- **EPSS (FIRST.org)** : **probabilité prédictive** d'exploitation sous 30 jours.

---

## 🛠️ Stack surveillée

- **Java / Spring** : `spring`, `spring-boot`, `spring-security`, `tomcat`, `hibernate`, `jackson`, `log4j`, `logback`
- **Frontend** : `angular`, `typescript`, `rxjs`, `node.js`, `npm`
- **Infrastructure** : `docker`, `kubernetes`, `postgresql`, `nginx`, `keycloak`

*Paramétrable dans [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml).*

---

## 🚀 Utilisation locale

```bash
# 1. Lancer la synchronisation quotidienne (Mode CLI Batch)
cd backend && mvn spring-boot:run -Dspring-boot.run.arguments="--sync"

# 2. Lancer les tests unitaires
cd backend && mvn test

# 3. Lancer le dashboard web localement
python3 -m http.server 3000 --directory frontend
# Ouvrir http://localhost:3000
```

---

## 🔐 Secrets GitHub (Optionnels)

Dans **Settings > Secrets and variables > Actions** :
- `DISCORD_WEBHOOK_URL` : Pour recevoir les alertes immédiates en cas de CVE critique ou KEV sur Discord.
- `NVD_API_KEY` : Clé API NIST gratuite (augmente le débit de requêtes).

---

## 📜 Licence
Projet distribué sous licence [MIT](LICENSE).
