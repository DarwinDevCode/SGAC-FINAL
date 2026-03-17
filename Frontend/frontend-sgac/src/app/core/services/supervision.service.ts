import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Ayudante,
  EvaluacionActividadRequest,
  EvaluacionEvidenciaRequest,
  RegistroActividad,
} from '../../features/docente/mis-ayudantes/ayudantes.model';

@Injectable({ providedIn: 'root' })
export class SupervisionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getMisAyudantes() {
    return this.http.get<Ayudante[]>(`${this.baseUrl}/docente/mis-ayudantes`).pipe(
      catchError((err) => {
        const message = err?.error?.message || err?.message || 'Error al cargar mis ayudantes.';
        return throwError(() => new Error(message));
      })
    );
  }

  getActividadesAyudante(idAyudantia: number) {
    return this.http
      .get<RegistroActividad[]>(`${this.baseUrl}/docente/ayudante/${idAyudantia}/actividades`)
      .pipe(
        catchError((err) => {
          const message = err?.error?.message || err?.message || 'Error al cargar actividades del ayudante.';
          return throwError(() => new Error(message));
        })
      );
  }

  evaluarActividad(idActividad: number, evaluacion: EvaluacionActividadRequest) {
    return this.http
      .put<void>(`${this.baseUrl}/docente/actividad/${idActividad}/evaluar`, evaluacion)
      .pipe(
        catchError((err) => {
          const message = err?.error?.message || err?.message || 'No se pudo guardar la evaluación de la actividad.';
          return throwError(() => new Error(message));
        })
      );
  }

  evaluarEvidencia(idEvidencia: number, evaluacion: EvaluacionEvidenciaRequest) {
    return this.http
      .put<void>(`${this.baseUrl}/docente/evidencia/${idEvidencia}/evaluar`, evaluacion)
      .pipe(
        catchError((err) => {
          const message = err?.error?.message || err?.message || 'No se pudo guardar la evaluación de la evidencia.';
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Lógica simple:
   * - si viene un http(s), lo abre en una nueva pestaña.
   * - si viene una ruta local, consume el endpoint de descarga autenticado.
   */
  descargarEvidencia(rutaArchivo: string, idEvidencia?: number) {
    if (!rutaArchivo && idEvidencia == null) return;

    if (rutaArchivo?.startsWith('http://') || rutaArchivo?.startsWith('https://')) {
      window.open(rutaArchivo, '_blank');
      return;
    }

    if (idEvidencia == null) return;

    const url = `${this.baseUrl}/docente/evidencia/${idEvidencia}/download`;
    window.open(url, '_blank');
  }
}

