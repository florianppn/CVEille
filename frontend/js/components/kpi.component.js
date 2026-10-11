/**
 * CVEille • KPI Cards Component
 * Updates the high-level intelligence counters and synchronization status
 */

import { formatSyncDate, isWithinPastHours } from '../utils/date.utils.js';

export class KpiComponent {
  constructor() {
    this.syncTextEl = document.getElementById('last-sync-text');
    this.kpiNewEl = document.getElementById('kpi-new');
    this.kpiKevEl = document.getElementById('kpi-kev');
    this.kpiCriticalEl = document.getElementById('kpi-critical');
    this.kpiHighEl = document.getElementById('kpi-high');
  }

  render(store) {
    if (this.syncTextEl) {
      this.syncTextEl.textContent = formatSyncDate(store.lastSync);
    }

    if (this.kpiNewEl) {
      if (typeof store.newCount === 'number') {
        this.kpiNewEl.textContent = store.newCount;
      } else {
        // Fallback: calculate CVEs published in the past 48 hours
        const count48h = store.allCves.filter(c => isWithinPastHours(c.publishedDate, 48)).length;
        this.kpiNewEl.textContent = count48h;
      }
    }

    const stats = store.stats;
    if (!stats) return;

    if (this.kpiKevEl) this.kpiKevEl.textContent = stats.kevCount || 0;
    if (this.kpiCriticalEl) this.kpiCriticalEl.textContent = stats.criticalCount || 0;
    if (this.kpiHighEl) this.kpiHighEl.textContent = stats.highCount || 0;
  }
}
