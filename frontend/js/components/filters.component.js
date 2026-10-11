/**
 * CVEille • Search & Filter Controls Component
 */

export class FiltersComponent {
  constructor(store, onReset) {
    this.store = store;
    this.onReset = onReset;

    this.searchInput = document.getElementById('searchInput');
    this.clearSearchBtn = document.getElementById('clearSearchBtn');
    this.severitySelect = document.getElementById('severitySelect');
    this.dateSortSelect = document.getElementById('dateSortSelect');
    this.resetBtn = document.getElementById('resetFiltersBtn');

    this.setupListeners();
  }

  setupListeners() {
    if (this.searchInput) {
      this.searchInput.addEventListener('input', () => {
        const query = this.searchInput.value;
        this.updateClearBtn(query);
        this.store.setQuery(query);
      });
    }

    if (this.clearSearchBtn) {
      this.clearSearchBtn.addEventListener('click', () => {
        if (this.searchInput) {
          this.searchInput.value = '';
          this.searchInput.focus();
        }
        this.updateClearBtn('');
        this.store.setQuery('');
      });
    }

    if (this.severitySelect) {
      this.severitySelect.addEventListener('change', () => {
        this.store.setSeverity(this.severitySelect.value);
      });
    }

    if (this.dateSortSelect) {
      this.dateSortSelect.addEventListener('change', () => {
        this.store.setDateSort(this.dateSortSelect.value);
      });
    }

    if (this.resetBtn) {
      this.resetBtn.addEventListener('click', () => {
        if (this.searchInput) this.searchInput.value = '';
        if (this.severitySelect) this.severitySelect.value = 'ALL';
        if (this.dateSortSelect) this.dateSortSelect.value = 'DEFAULT';
        this.updateClearBtn('');

        this.store.resetFilters();
        if (typeof this.onReset === 'function') {
          this.onReset();
        }
      });
    }
  }

  updateClearBtn(query) {
    if (this.clearSearchBtn) {
      this.clearSearchBtn.style.display = query ? 'inline-block' : 'none';
    }
  }

  render(store) {
    if (this.resetBtn) {
      this.resetBtn.style.display = store.isFiltered() ? 'inline-block' : 'none';
    }
  }
}
