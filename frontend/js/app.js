/**
 * CVEille • Application Entrypoint & Orchestrator
 * Modular Vanilla ES6 Architecture
 */

import { ApiService } from './services/api.service.js';
import { Store } from './state/store.js';
import { KpiComponent } from './components/kpi.component.js';
import { ChartsComponent } from './components/charts.component.js';
import { ChipsComponent } from './components/chips.component.js';
import { FiltersComponent } from './components/filters.component.js';
import { TableComponent } from './components/table.component.js';

export class App {
  constructor() {
    this.api = new ApiService();
    this.store = new Store();

    this.kpi = new KpiComponent();
    this.charts = new ChartsComponent();
    this.table = new TableComponent();

    this.chips = new ChipsComponent((tech) => {
      this.store.setTech(tech);
    });

    this.filters = new FiltersComponent(this.store, () => {
      this.chips.resetActive();
    });
  }

  async start() {
    // 1. Subscribe dynamic components to store updates
    this.store.subscribe((store) => {
      this.kpi.render(store);
      this.filters.render(store);
      this.table.render(store.filteredCves);
    });

    // 2. Fetch dataset and initialize visualizations
    try {
      const data = await this.api.fetchIndexData();
      this.store.setData(data);

      // Render charts & chips based on full aggregated dataset
      this.charts.render(this.store.stats);
      this.chips.render(this.store.stats, this.store.filters.tech);
    } catch (err) {
      console.error('Erreur chargement données CVEille:', err);
      this.table.renderError('Impossible de charger les données de veille (data/index.json).');
      const syncEl = document.getElementById('last-sync-text');
      if (syncEl) syncEl.textContent = 'Données indisponibles';
    }
  }
}

// Bootstrap on DOM ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    new App().start();
  });
} else {
  new App().start();
}
