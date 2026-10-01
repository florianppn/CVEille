'use strict';

/**
 * CVEille • Dashboard Logic
 * Modular, lightweight JavaScript (Vanilla)
 */

(function () {
  let allCves = [];
  let statsData = null;
  let activeTech = 'ALL';
  let severityChart = null;
  let techChart = null;

  // DOM Elements Cache
  const elements = {
    syncText: document.getElementById('last-sync-text'),
    kpiTotal: document.getElementById('kpi-total'),
    kpiKev: document.getElementById('kpi-kev'),
    kpiCritical: document.getElementById('kpi-critical'),
    kpiHigh: document.getElementById('kpi-high'),
    searchInput: document.getElementById('searchInput'),
    clearSearchBtn: document.getElementById('clearSearchBtn'),
    severitySelect: document.getElementById('severitySelect'),
    kevCheckbox: document.getElementById('kevOnlyCheckbox'),
    kevToggleLabel: document.getElementById('kevToggleLabel'),
    resetBtn: document.getElementById('resetFiltersBtn'),
    chipsContainer: document.getElementById('techChipsContainer'),
    tableBody: document.getElementById('cveTableBody'),
    resultsCount: document.getElementById('resultsCount')
  };

  document.addEventListener('DOMContentLoaded', initApp);

  function initApp() {
    setupEventListeners();
    fetchData();
  }

  function fetchData() {
    fetch('data/index.json')
      .then((res) => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
      })
      .then((data) => {
        statsData = data.stats || null;
        allCves = Array.isArray(data.cves) ? data.cves : [];

        renderSyncDate(data.lastSync || (statsData ? statsData.lastSyncTimestamp : null));
        renderKpis(statsData);
        renderCharts(statsData);
        renderChips(statsData);
        applyFilters();
      })
      .catch((err) => {
        console.error('Erreur chargement index.json:', err);
        elements.syncText.textContent = 'Données non disponibles';
        elements.tableBody.innerHTML = '<tr><td colspan="8" class="table-empty">Impossible de charger data/index.json. Exécutez une synchronisation.</td></tr>';
      });
  }

  function renderSyncDate(isoDate) {
    if (!isoDate) {
      elements.syncText.textContent = 'Sync inconnue';
      return;
    }
    const d = new Date(isoDate);
    const formatted = d.toLocaleDateString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
    elements.syncText.textContent = 'Sync : ' + formatted;
  }

  function renderKpis(stats) {
    if (!stats) return;
    elements.kpiTotal.textContent = stats.totalTracked || 0;
    elements.kpiKev.textContent = stats.kevCount || 0;
    elements.kpiCritical.textContent = stats.criticalCount || 0;
    elements.kpiHigh.textContent = stats.highCount || 0;
  }

  function renderCharts(stats) {
    if (!stats || typeof Chart === 'undefined') return;

    // 1. Severity Doughnut Chart
    const sevCanvas = document.getElementById('severityChart');
    if (sevCanvas) {
      if (severityChart) severityChart.destroy();
      const dist = stats.severityDistribution || {};
      severityChart = new Chart(sevCanvas, {
        type: 'doughnut',
        data: {
          labels: ['Critique', 'Élevée', 'Moyenne', 'Basse'],
          datasets: [{
            data: [dist.CRITICAL || 0, dist.HIGH || 0, dist.MEDIUM || 0, dist.LOW || 0],
            backgroundColor: ['#ef4444', '#f97316', '#eab308', '#3b82f6'],
            borderWidth: 2,
            borderColor: '#090d16'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom',
              labels: { color: '#94a3b8', font: { size: 11 } }
            }
          }
        }
      });
    }

    // 2. Tech Bar Chart
    const techCanvas = document.getElementById('techChart');
    if (techCanvas) {
      if (techChart) techChart.destroy();
      const techData = stats.topTechnologies || {};
      const labels = Object.keys(techData).slice(0, 6);
      const values = labels.map(k => techData[k]);

      techChart = new Chart(techCanvas, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            data: values,
            backgroundColor: '#38bdf8',
            borderRadius: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              ticks: { color: '#94a3b8', font: { size: 11 } },
              grid: { display: false }
            },
            y: {
              ticks: { color: '#94a3b8', stepSize: 1 },
              grid: { color: 'rgba(255, 255, 255, 0.05)' }
            }
          },
          plugins: { legend: { display: false } }
        }
      });
    }
  }

  function renderChips(stats) {
    if (!stats || !stats.topTechnologies) return;
    const techs = Object.keys(stats.topTechnologies);

    let html = '<button class="chip-tag active" data-tech="ALL">Tous</button>';
    techs.forEach(t => {
      html += `<button class="chip-tag" data-tech="${escapeHtml(t)}">${escapeHtml(t)}</button>`;
    });

    elements.chipsContainer.innerHTML = html;

    elements.chipsContainer.querySelectorAll('.chip-tag').forEach(btn => {
      btn.addEventListener('click', function () {
        const tech = this.getAttribute('data-tech');
        activeTech = tech;

        elements.chipsContainer.querySelectorAll('.chip-tag').forEach(b => b.classList.remove('active'));
        this.classList.add('active');

        applyFilters();
      });
    });
  }

  function applyFilters() {
    const query = elements.searchInput.value.trim().toLowerCase();
    const severity = elements.severitySelect.value;
    const kevOnly = elements.kevCheckbox.checked;

    elements.clearSearchBtn.style.display = query ? 'inline-block' : 'none';
    elements.kevToggleLabel.classList.toggle('active', kevOnly);

    const isFiltered = query !== '' || severity !== 'ALL' || kevOnly || activeTech !== 'ALL';
    elements.resetBtn.style.display = isFiltered ? 'inline-block' : 'none';

    const filtered = allCves.filter(cve => {
      if (kevOnly && !cve.inKev) return false;
      if (severity !== 'ALL' && (cve.severity || '').toUpperCase() !== severity) return false;
      if (activeTech !== 'ALL') {
        if (!cve.matchedKeywords || !cve.matchedKeywords.includes(activeTech)) return false;
      }
      if (query) {
        const idMatch = (cve.id || '').toLowerCase().includes(query);
        const descMatch = (cve.description || '').toLowerCase().includes(query);
        const techMatch = Array.isArray(cve.matchedKeywords) && cve.matchedKeywords.some(t => t.toLowerCase().includes(query));
        return idMatch || descMatch || techMatch;
      }
      return true;
    });

    elements.resultsCount.textContent = filtered.length;
    renderTable(filtered);
  }

  function renderTable(cves) {
    if (!cves || cves.length === 0) {
      elements.tableBody.innerHTML = '<tr><td colspan="8" class="table-empty">Aucune vulnérabilité ne correspond aux critères sélectionnés.</td></tr>';
      return;
    }

    const rows = cves.map(cve => {
      const sevClass = 'badge-' + (cve.severity ? cve.severity.toLowerCase() : 'unknown');
      const kevBadge = cve.inKev
        ? '<span class="badge badge-kev" title="Présente au catalogue officiel CISA KEV">🔥 EXPLOITÉ</span>'
        : '<span class="badge badge-safe">Non</span>';

      const cvssText = cve.cvssScore !== null && cve.cvssScore !== undefined
        ? `<strong>${cve.cvssScore.toFixed(1)}</strong>`
        : '<span style="color:var(--text-dim)">N/A</span>';

      const epssText = cve.epssScore !== null && cve.epssScore !== undefined
        ? `<span class="epss-badge">${(cve.epssScore * 100).toFixed(1)}%</span>`
        : '<span style="color:var(--text-dim)">N/A</span>';

      const tagsHtml = Array.isArray(cve.matchedKeywords)
        ? cve.matchedKeywords.map(t => `<span class="pill-tag">${escapeHtml(t)}</span>`).join('')
        : '';

      const dateStr = cve.publishedDate ? cve.publishedDate.substring(0, 10) : '';

      return `
        <tr class="${cve.inKev ? 'row-kev' : ''}">
          <td><a href="${escapeHtml(cve.nvdUrl || '#')}" target="_blank" rel="noopener" class="cve-link">${escapeHtml(cve.id)}</a></td>
          <td><span class="badge ${sevClass}">${escapeHtml(cve.severity || 'N/A')}</span></td>
          <td>${cvssText}</td>
          <td>${epssText}</td>
          <td>${kevBadge}</td>
          <td>${tagsHtml}</td>
          <td class="desc-text" title="${escapeHtml(cve.description || '')}">${escapeHtml(cve.description || '')}</td>
          <td style="white-space:nowrap;color:var(--text-dim);font-size:0.78rem;">${dateStr}</td>
        </tr>
      `;
    });

    elements.tableBody.innerHTML = rows.join('');
  }

  function setupEventListeners() {
    elements.searchInput.addEventListener('input', applyFilters);
    elements.severitySelect.addEventListener('change', applyFilters);
    elements.kevCheckbox.addEventListener('change', applyFilters);

    elements.clearSearchBtn.addEventListener('click', () => {
      elements.searchInput.value = '';
      applyFilters();
      elements.searchInput.focus();
    });

    elements.resetBtn.addEventListener('click', () => {
      elements.searchInput.value = '';
      elements.severitySelect.value = 'ALL';
      elements.kevCheckbox.checked = false;
      activeTech = 'ALL';
      elements.chipsContainer.querySelectorAll('.chip-tag').forEach(b => {
        b.classList.toggle('active', b.getAttribute('data-tech') === 'ALL');
      });
      applyFilters();
    });
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
})();
