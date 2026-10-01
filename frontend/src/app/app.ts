import { Component, OnInit, signal, computed, ElementRef, viewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CveService } from './services/cve.service';
import { IndexData, EnrichedCve, CveStats } from './models/cve.model';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  private cveService = inject(CveService);

  severityCanvas = viewChild<ElementRef<HTMLCanvasElement>>('severityCanvas');
  techCanvas = viewChild<ElementRef<HTMLCanvasElement>>('techCanvas');

  data = signal<IndexData | null>(null);
  loading = signal<boolean>(true);
  searchQuery = signal<string>('');
  selectedSeverity = signal<string>('ALL');
  kevOnly = signal<boolean>(false);
  selectedTech = signal<string>('ALL');

  private severityChart: Chart | null = null;
  private techChart: Chart | null = null;

  stats = computed<CveStats | null>(() => this.data()?.stats ?? null);

  uniqueTechs = computed<string[]>(() => {
    const d = this.data();
    if (!d || !d.stats?.topTechnologies) return [];
    return Object.keys(d.stats.topTechnologies);
  });

  filteredCves = computed<EnrichedCve[]>(() => {
    const d = this.data();
    if (!d || !d.cves) return [];

    const query = this.searchQuery().trim().toLowerCase();
    const severity = this.selectedSeverity();
    const kev = this.kevOnly();
    const tech = this.selectedTech();

    return d.cves.filter(cve => {
      if (kev && !cve.inKev) return false;
      if (severity !== 'ALL' && cve.severity?.toUpperCase() !== severity) return false;
      if (tech !== 'ALL') {
        if (!cve.matchedKeywords || !cve.matchedKeywords.includes(tech)) return false;
      }
      if (query) {
        const idMatch = cve.id.toLowerCase().includes(query);
        const descMatch = cve.description?.toLowerCase().includes(query);
        const techMatch = cve.matchedKeywords?.some(t => t.toLowerCase().includes(query));
        return idMatch || descMatch || techMatch;
      }
      return true;
    });
  });

  ngOnInit() {
    this.cveService.getIndexData().subscribe({
      next: (result) => {
        this.data.set(result);
        this.loading.set(false);
        setTimeout(() => this.initCharts(), 100);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  setTechFilter(tech: string) {
    if (this.selectedTech() === tech) {
      this.selectedTech.set('ALL');
    } else {
      this.selectedTech.set(tech);
    }
  }

  resetFilters() {
    this.searchQuery.set('');
    this.selectedSeverity.set('ALL');
    this.kevOnly.set(false);
    this.selectedTech.set('ALL');
  }

  private initCharts() {
    const currentStats = this.stats();
    if (!currentStats) return;

    // 1. Severity Doughnut Chart
    const sevCanvas = this.severityCanvas()?.nativeElement;
    if (sevCanvas) {
      if (this.severityChart) this.severityChart.destroy();

      const sevData = currentStats.severityDistribution || {};
      const labels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
      const counts = labels.map(l => sevData[l] || 0);

      this.severityChart = new Chart(sevCanvas, {
        type: 'doughnut',
        data: {
          labels: ['Critique', 'Élevée', 'Moyenne', 'Basse'],
          datasets: [{
            data: counts,
            backgroundColor: ['#ef4444', '#f97316', '#eab308', '#3b82f6'],
            borderWidth: 2,
            borderColor: '#0f172a'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom',
              labels: { color: '#94a3b8', font: { size: 12, family: 'Inter, system-ui' } }
            }
          }
        }
      });
    }

    // 2. Tech Bar Chart
    const tCanvas = this.techCanvas()?.nativeElement;
    if (tCanvas) {
      if (this.techChart) this.techChart.destroy();

      const techData = currentStats.topTechnologies || {};
      const techLabels = Object.keys(techData).slice(0, 7);
      const techCounts = techLabels.map(k => techData[k]);

      this.techChart = new Chart(tCanvas, {
        type: 'bar',
        data: {
          labels: techLabels,
          datasets: [{
            label: 'Vulnérabilités',
            data: techCounts,
            backgroundColor: '#38bdf8',
            borderRadius: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              ticks: { color: '#94a3b8', font: { family: 'Inter, system-ui' } },
              grid: { display: false }
            },
            y: {
              ticks: { color: '#94a3b8', stepSize: 1 },
              grid: { color: 'rgba(148, 163, 184, 0.1)' }
            }
          },
          plugins: {
            legend: { display: false }
          }
        }
      });
    }
  }
}

