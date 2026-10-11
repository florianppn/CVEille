/**
 * CVEille • State Store
 * Centralized reactive state container with multi-criteria filtering and sorting
 */

import { StatsService } from '../services/stats.service.js';

export class Store {
  constructor() {
    this.allCves = [];
    this.filteredCves = [];
    this.stats = null;
    this.newCount = 0;
    this.lastSync = null;

    this.filters = {
      query: '',
      severity: 'ALL',
      tech: 'ALL',
      dateSort: 'DEFAULT'
    };

    this.currentPage = 1;
    this.pageSize = 10;

    this.listeners = [];
  }

  subscribe(listener) {
    if (typeof listener === 'function') {
      this.listeners.push(listener);
    }
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  notify() {
    for (const listener of this.listeners) {
      listener(this);
    }
  }

  setData({ cves = [], stats = null, newToday = null, lastSync = null }) {
    this.allCves = Array.isArray(cves) ? cves : [];
    this.lastSync = lastSync;
    this.newCount = (typeof newToday === 'number') ? newToday : null;
    this.stats = StatsService.compute(this.allCves);

    this.applyFilters();
  }

  setQuery(query) {
    this.filters.query = (query || '').trim().toLowerCase();
    this.applyFilters();
  }

  setSeverity(severity) {
    this.filters.severity = severity || 'ALL';
    this.applyFilters();
  }

  setTech(tech) {
    this.filters.tech = tech || 'ALL';
    this.applyFilters();
  }

  setDateSort(dateSort) {
    this.filters.dateSort = dateSort || 'DEFAULT';
    this.applyFilters();
  }

  resetFilters() {
    this.filters.query = '';
    this.filters.severity = 'ALL';
    this.filters.tech = 'ALL';
    this.filters.dateSort = 'DEFAULT';
    this.applyFilters();
  }

  isFiltered() {
    return (
      this.filters.query !== '' ||
      this.filters.severity !== 'ALL' ||
      this.filters.tech !== 'ALL' ||
      this.filters.dateSort !== 'DEFAULT'
    );
  }

  applyFilters() {
    const { query, severity, tech, dateSort } = this.filters;

    let result = this.allCves.filter(cve => {
      // 1. Severity filter
      if (severity !== 'ALL' && (cve.severity || '').toUpperCase() !== severity) {
        return false;
      }

      // 2. Tech stack filter
      if (tech !== 'ALL') {
        if (!cve.matchedKeywords || !cve.matchedKeywords.includes(tech)) {
          return false;
        }
      }

      // 3. Text query filter
      if (query) {
        const idMatch = (cve.id || '').toLowerCase().includes(query);
        const descMatch = (cve.description || '').toLowerCase().includes(query);
        const techMatch = Array.isArray(cve.matchedKeywords) &&
          cve.matchedKeywords.some(t => t.toLowerCase().includes(query));
        if (!idMatch && !descMatch && !techMatch) {
          return false;
        }
      }

      return true;
    });

    // 4. Date sorting
    if (dateSort === 'DESC') {
      result.sort((a, b) => {
        const timeA = a.publishedDate ? new Date(a.publishedDate).getTime() : 0;
        const timeB = b.publishedDate ? new Date(b.publishedDate).getTime() : 0;
        return timeB - timeA;
      });
    } else if (dateSort === 'ASC') {
      result.sort((a, b) => {
        const timeA = a.publishedDate ? new Date(a.publishedDate).getTime() : 0;
        const timeB = b.publishedDate ? new Date(b.publishedDate).getTime() : 0;
        return timeA - timeB;
      });
    }

    this.filteredCves = result;
    this.currentPage = 1;
    this.notify();
  }

  setPage(page) {
    const totalPages = this.getTotalPages();
    const targetPage = Math.max(1, Math.min(page, totalPages));
    if (targetPage !== this.currentPage) {
      this.currentPage = targetPage;
      this.notify();
    }
  }

  getTotalPages() {
    return Math.max(1, Math.ceil(this.filteredCves.length / this.pageSize));
  }

  getPaginatedCves() {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredCves.slice(start, start + this.pageSize);
  }

  getPaginationInfo() {
    const totalItems = this.filteredCves.length;
    const totalPages = this.getTotalPages();
    const startItem = totalItems === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
    const endItem = Math.min(this.currentPage * this.pageSize, totalItems);
    return {
      currentPage: this.currentPage,
      totalPages,
      totalItems,
      startItem,
      endItem,
      pageSize: this.pageSize
    };
  }
}
