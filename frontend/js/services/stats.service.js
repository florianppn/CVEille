/**
 * CVEille • Statistical Aggregation Service
 * Pure mathematical computation decoupled from DOM representation
 */

export class StatsService {
  /**
   * Computes accurate aggregation metrics across a CVE collection.
   * @param {Array} cves - Collection of CVE records
   * @returns {Object} Computed statistics
   */
  static compute(cves = []) {
    let critical = 0;
    let high = 0;
    let medium = 0;
    let low = 0;
    let kev = 0;
    const techCounts = {};

    for (const cve of cves) {
      const sev = (cve.severity || '').toUpperCase();
      if (sev === 'CRITICAL') critical++;
      else if (sev === 'HIGH') high++;
      else if (sev === 'MEDIUM') medium++;
      else if (sev === 'LOW') low++;

      if (cve.inKev) kev++;

      if (Array.isArray(cve.matchedKeywords)) {
        for (const t of cve.matchedKeywords) {
          techCounts[t] = (techCounts[t] || 0) + 1;
        }
      }
    }

    return {
      totalTracked: cves.length,
      criticalCount: critical,
      highCount: high,
      mediumCount: medium,
      lowCount: low,
      kevCount: kev,
      severityDistribution: {
        CRITICAL: critical,
        HIGH: high,
        MEDIUM: medium,
        LOW: low
      },
      topTechnologies: techCounts
    };
  }
}
