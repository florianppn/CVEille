import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { IndexData } from '../models/cve.model';

@Injectable({
  providedIn: 'root'
})
export class CveService {
  private http = inject(HttpClient);

  getIndexData(): Observable<IndexData | null> {
    // In production (GitHub Pages) or local build, data/index.json is available in assets/public
    return this.http.get<IndexData>('data/index.json').pipe(
      catchError(err => {
        console.warn('Could not load data/index.json, trying fallback or local API...', err);
        return this.http.get<IndexData>('assets/data/index.json').pipe(
          catchError(() => {
            console.error('Failed to load CVE index data.');
            return of(null);
          })
        );
      })
    );
  }
}
