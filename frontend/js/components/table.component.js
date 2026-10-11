/**
 * CVEille • CVE Vulnerabilities Table Component
 * Renders CVE records with badges, scores, tags, and formatted date + time
 */

import { escapeHtml } from '../utils/dom.utils.js';
import { formatPublishDate } from '../utils/date.utils.js';

export class TableComponent {
  constructor() {
    this.tableBody = document.getElementById('cveTableBody');
    this.resultsCount = document.getElementById('resultsCount');
  }

  render(cves = []) {
    if (this.resultsCount) {
      this.resultsCount.textContent = cves.length;
    }

    if (!this.tableBody) return;

    if (cves.length === 0) {
      this.tableBody.innerHTML = '<tr><td colspan="8" class="table-empty">Aucune vulnérabilité ne correspond aux critères.</td></tr>';
      return;
    }

    const rows = cves.map(cve => {
      const sevClass = 'badge-' + (cve.severity ? cve.severity.toLowerCase() : 'unknown');
      const kevBadge = cve.inKev
        ? '<span class="badge badge-kev">Oui</span>'
        : '<span class="badge-safe">—</span>';

      const cvssText = (cve.cvssScore !== null && cve.cvssScore !== undefined)
        ? `<span class="score-cell">${cve.cvssScore.toFixed(1)}</span>`
        : '<span style="color:var(--text-tertiary)">—</span>';

      const epssText = (cve.epssScore !== null && cve.epssScore !== undefined)
        ? `<span class="epss-badge">${(cve.epssScore * 100).toFixed(1)}%</span>`
        : '<span style="color:var(--text-tertiary)">—</span>';

      const tagsHtml = Array.isArray(cve.matchedKeywords)
        ? cve.matchedKeywords.map(t => `<span class="pill-tag">${escapeHtml(t)}</span>`).join('')
        : '';

      const formattedDateTime = formatPublishDate(cve.publishedDate);

      return `
        <tr class="${cve.inKev ? 'row-kev' : ''}">
          <td><a href="${escapeHtml(cve.nvdUrl || '#')}" target="_blank" rel="noopener" class="cve-link">${escapeHtml(cve.id)}</a></td>
          <td><span class="badge ${sevClass}">${escapeHtml(cve.severity || 'N/A')}</span></td>
          <td>${cvssText}</td>
          <td>${epssText}</td>
          <td>${kevBadge}</td>
          <td>${tagsHtml}</td>
          <td class="desc-text" title="${escapeHtml(cve.description || '')}">${escapeHtml(cve.description || '')}</td>
          <td style="white-space:nowrap;color:var(--text-tertiary);font-size:0.75rem;font-family:var(--font-mono);">${formattedDateTime}</td>
        </tr>
      `;
    });

    this.tableBody.innerHTML = rows.join('');
  }

  renderError(message) {
    if (this.tableBody) {
      this.tableBody.innerHTML = `<tr><td colspan="8" class="table-empty">${escapeHtml(message)}</td></tr>`;
    }
  }
}
