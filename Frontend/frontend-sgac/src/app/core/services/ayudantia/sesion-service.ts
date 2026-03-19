// core/services/sesion-service.ts
// ═══════════════════════════════════════════════════════════════════
// Servicio de sesiones — fusiona la API antigua (listarMisSesiones,
// obtenerDetalleMiSesion) con los tres nuevos endpoints del backend
// refactorizado (planificarSesion, completarSesion, evaluarSesion).
// ═══════════════════════════════════════════════════════════════════

import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

import {
  SesionResponseDTO,
  PlanificarSesionRequest,
  PlanificarSesionResponse,
  CompletarSesionRequest,
  CompletarSesionResponse,
  EvaluarSesionRequest,
  EvaluarSesionResponse,
} from '../../models/ayudantia/Sesiones';

@Injectable({ providedIn: 'root' })
export class SesionService {

  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/sesiones`;

  listarMisSesiones(
    _idAyudante?: number,
    fechaDesde?:  string,
    fechaHasta?:  string,
    estadoCodigo?: string
  ): Observable<SesionResponseDTO[]> {
    let params = new HttpParams();
    if (fechaDesde)    params = params.set('fechaDesde',   fechaDesde);
    if (fechaHasta)    params = params.set('fechaHasta',   fechaHasta);
    if (estadoCodigo)  params = params.set('estadoCodigo', estadoCodigo);
    return this.http.get<SesionResponseDTO[]>(`${this.base}/mis-sesiones`, { params });
  }

  /**
   * Detalle completo de una sesión (incluye la lista de evidencias).
   * El `_idAyudante` se ignora en el backend pero se mantiene para no
   * romper las llamadas existentes.
   */
  obtenerDetalleMiSesion(
    _idAyudante: number,
    idRegistroActividad: number
  ): Observable<SesionResponseDTO> {
    return this.http.get<SesionResponseDTO>(`${this.base}/${idRegistroActividad}`);
  }

  // ── Escritura — nuevos endpoints ─────────────────────────────────────

  /**
   * Crea una sesión en estado PLANIFICADO.
   * El backend notifica al docente automáticamente (efecto panóptico).
   */
  planificarSesion(request: PlanificarSesionRequest): Observable<PlanificarSesionResponse> {
    return this.http.post<PlanificarSesionResponse>(`${this.base}/planificar`, request);
  }

  /**
   * Envía una sesión a revisión (PLANIFICADO / RECHAZADO → PENDIENTE).
   * Usa multipart/form-data para combinar JSON y archivos binarios.
   */
  completarSesion(
    idRegistro: number,
    request:    CompletarSesionRequest,
    archivos:   File[]
  ): Observable<CompletarSesionResponse> {
    const fd = new FormData();
    // El backend espera la parte "datos" como application/json
    fd.append('datos', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    archivos.forEach(f => fd.append('archivos', f, f.name));
    return this.http.post<CompletarSesionResponse>(`${this.base}/${idRegistro}/completar`, fd);
  }

  /**
   * El docente aprueba o rechaza una sesión en estado PENDIENTE.
   * Al aprobar, las horas se acumulan en la ayudantía.
   */
  evaluarSesion(
    idRegistro: number,
    request:    EvaluarSesionRequest
  ): Observable<EvaluarSesionResponse> {
    return this.http.put<EvaluarSesionResponse>(`${this.base}/${idRegistro}/evaluar`, request);
  }
}
