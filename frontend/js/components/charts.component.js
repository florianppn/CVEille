/**
 * CVEille • Charts Component
 * Encapsulates Chart.js lifecycle, configuration, and data visualizers
 */

export class ChartsComponent {
  constructor() {
    this.severityCanvas = document.getElementById('severityChart');
    this.techCanvas = document.getElementById('techChart');

    this.severityChart = null;
    this.techChart = null;
  }

  render(stats) {
    if (!stats || typeof Chart === 'undefined') return;

    this.renderSeverityChart(stats);
    this.renderTechChart(stats);
  }

  renderSeverityChart(stats) {
    if (!this.severityCanvas) return;

    if (this.severityChart) {
      this.severityChart.destroy();
      this.severityChart = null;
    }

    const dist = stats.severityDistribution || {};
    this.severityChart = new Chart(this.severityCanvas, {
      type: 'doughnut',
      data: {
        labels: ['Critique', 'Élevée', 'Moyenne', 'Basse'],
        datasets: [{
          data: [dist.CRITICAL || 0, dist.HIGH || 0, dist.MEDIUM || 0, dist.LOW || 0],
          backgroundColor: ['#ef4444', '#f97316', '#eab308', '#60a5fa'],
          borderWidth: 2,
          borderColor: '#121215'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#a1a1aa', font: { size: 11 } }
          }
        }
      }
    });
  }

  renderTechChart(stats) {
    if (!this.techCanvas) return;

    if (this.techChart) {
      this.techChart.destroy();
      this.techChart = null;
    }

    const techData = stats.topTechnologies || {};
    const labels = Object.keys(techData).slice(0, 8);
    const values = labels.map(k => techData[k]);

    this.techChart = new Chart(this.techCanvas, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: '#3f3f46',
          hoverBackgroundColor: '#71717a',
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            ticks: { color: '#71717a', font: { size: 11, family: 'ui-monospace, monospace' } },
            grid: { display: false }
          },
          y: {
            ticks: { color: '#71717a', stepSize: 1 },
            grid: { color: 'rgba(255, 255, 255, 0.04)' }
          }
        },
        plugins: { legend: { display: false } }
      }
    });
  }
}
