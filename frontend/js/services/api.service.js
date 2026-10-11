/**
 * CVEille • API Service
 * Manages HTTP communications and caching strategies
 */

export class ApiService {
  constructor(endpoint = 'data/index.json') {
    this.endpoint = endpoint;
  }

  /**
   * Fetches latest aggregated CVE dataset with cache-busting.
   * @returns {Promise<{cves: Array, stats: Object, newToday: number, lastSync: string}>}
   */
  async fetchIndexData() {
    const url = `${this.endpoint}?t=${Date.now()}`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error ${response.status} fetching ${url}`);
    }
    return response.json();
  }
}
