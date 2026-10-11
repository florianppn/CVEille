/**
 * CVEille • Pagination Component
 * Manages table pagination controls, page slicing, and pagination metrics
 */

export class PaginationComponent {
  constructor(store) {
    this.store = store;
    this.container = document.getElementById('paginationContainer');
    this.infoElement = document.getElementById('paginationInfo');
    this.navElement = document.getElementById('paginationNav');

    this.bindEvents();
  }

  bindEvents() {
    if (!this.navElement) return;

    this.navElement.addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-page]');
      if (!btn || btn.disabled) return;

      const targetPage = parseInt(btn.dataset.page, 10);
      if (!isNaN(targetPage) && targetPage > 0) {
        this.store.setPage(targetPage);

        // Optional smooth scroll to top of table
        const tableSection = document.querySelector('.table-section');
        if (tableSection) {
          const rect = tableSection.getBoundingClientRect();
          if (rect.top < 0) {
            tableSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }
      }
    });
  }

  render(store) {
    if (!this.container || !this.infoElement || !this.navElement) return;

    const info = store.getPaginationInfo();

    // 1. Render descriptive counter
    if (info.totalItems === 0) {
      this.infoElement.textContent = 'Aucune vulnérabilité trouvée';
      this.navElement.innerHTML = '';
      this.container.style.display = 'none';
      return;
    }

    this.container.style.display = 'flex';
    this.infoElement.textContent = `Affichage de ${info.startItem} à ${info.endItem} sur ${info.totalItems} vulnérabilité${info.totalItems > 1 ? 's' : ''}`;

    // If only 1 page, hide navigation buttons
    if (info.totalPages <= 1) {
      this.navElement.innerHTML = '';
      return;
    }

    // 2. Build pagination navigation buttons
    const html = [];

    // Previous Button
    const prevDisabled = info.currentPage <= 1 ? 'disabled' : '';
    html.push(`
      <button class="page-btn page-btn-prev" data-page="${info.currentPage - 1}" ${prevDisabled} aria-label="Page précédente">
        &larr; Précédent
      </button>
    `);

    // Numeric Page Buttons
    const pageNumbers = this.getPageNumbers(info.currentPage, info.totalPages);
    for (const p of pageNumbers) {
      if (p === '...') {
        html.push('<span class="page-ellipsis">&hellip;</span>');
      } else {
        const isActive = p === info.currentPage ? 'active' : '';
        html.push(`
          <button class="page-btn ${isActive}" data-page="${p}" aria-label="Page ${p}" ${p === info.currentPage ? 'aria-current="page"' : ''}>
            ${p}
          </button>
        `);
      }
    }

    // Next Button
    const nextDisabled = info.currentPage >= info.totalPages ? 'disabled' : '';
    html.push(`
      <button class="page-btn page-btn-next" data-page="${info.currentPage + 1}" ${nextDisabled} aria-label="Page suivante">
        Suivant &rarr;
      </button>
    `);

    this.navElement.innerHTML = html.join('');
  }

  getPageNumbers(current, total) {
    if (total <= 7) {
      const pages = [];
      for (let i = 1; i <= total; i++) pages.push(i);
      return pages;
    }

    if (current <= 4) {
      return [1, 2, 3, 4, 5, '...', total];
    }

    if (current >= total - 3) {
      return [1, '...', total - 4, total - 3, total - 2, total - 1, total];
    }

    return [1, '...', current - 1, current, current + 1, '...', total];
  }
}
