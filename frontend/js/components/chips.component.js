/**
 * CVEille • Technology Filter Chips Component
 */

import { escapeHtml } from '../utils/dom.utils.js';

export class ChipsComponent {
  constructor(onSelectTech) {
    this.container = document.getElementById('techChipsContainer');
    this.onSelectTech = onSelectTech;
  }

  render(stats, activeTech = 'ALL') {
    if (!this.container || !stats || !stats.topTechnologies) return;

    const techs = Object.keys(stats.topTechnologies);
    let html = `<button class="chip-tag ${activeTech === 'ALL' ? 'active' : ''}" data-tech="ALL">Tous</button>`;

    for (const t of techs) {
      const isActive = activeTech === t ? 'active' : '';
      html += `<button class="chip-tag ${isActive}" data-tech="${escapeHtml(t)}">${escapeHtml(t)}</button>`;
    }

    this.container.innerHTML = html;

    this.container.querySelectorAll('.chip-tag').forEach(btn => {
      btn.addEventListener('click', () => {
        const selected = btn.getAttribute('data-tech');
        this.container.querySelectorAll('.chip-tag').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        if (typeof this.onSelectTech === 'function') {
          this.onSelectTech(selected);
        }
      });
    });
  }

  resetActive() {
    if (!this.container) return;
    this.container.querySelectorAll('.chip-tag').forEach(b => {
      b.classList.toggle('active', b.getAttribute('data-tech') === 'ALL');
    });
  }
}
