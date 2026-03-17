import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DocenteDashboardDTO } from '../models/dashboard/docente-dashboard.model';

@Injectable({ providedIn: 'root' })
export class DocenteDashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getResumen() {
    return this.http
      .get<DocenteDashboardDTO>(`${this.baseUrl}/docente/dashboard/resumen`)
      .pipe(
        catchError((err) => {
          // Manejo básico: deja el error propagarse con un mensaje más claro
          const message = err?.error?.message || err?.message || 'Error al cargar el resumen del dashboard docente.';
          return throwError(() => new Error(message));
        })
      );
  }
}
