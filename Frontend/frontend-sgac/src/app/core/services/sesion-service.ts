import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

import {
  SesionResponseDTO,
  PlanificarSesionRequest,
  PlanificarSesionResponse,
  CompletarSesionRequest,
  CompletarSesionResponse,
  EvaluarSesionRequest,
  EvaluarSesionResponse,
} from '../models/Sesiones';
import {ControlSemanal} from '../dto/control-semanal';
import {ProgresoGeneral} from '../dto/progreso-general';

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

  obtenerDetalleMiSesion(
    _idAyudante: number,
    idRegistroActividad: number
  ): Observable<SesionResponseDTO> {
    return this.http.get<SesionResponseDTO>(`${this.base}/${idRegistroActividad}`);
  }

  planificarSesion(request: PlanificarSesionRequest): Observable<PlanificarSesionResponse> {
    return this.http.post<PlanificarSesionResponse>(`${this.base}/planificar`, request);
  }

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

  evaluarSesion(
    idRegistro: number,
    request:    EvaluarSesionRequest
  ): Observable<EvaluarSesionResponse> {
    return this.http.put<EvaluarSesionResponse>(`${this.base}/${idRegistro}/evaluar`, request);
  }

  progresoGeneral(idUsuario: number): Observable<ProgresoGeneral> {
    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.get<ProgresoGeneral>(`${this.base}/progreso`, { params });
  }

  controlSemanal(idUsuario: number): Observable<ControlSemanal> {
    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.get<ControlSemanal>(`${this.base}/control-semanal`, { params });
  }

  listarMisSesionesPrincipales(idAyudante: number): Observable<SesionResponseDTO[]> {
    const params = new HttpParams().set('idAyudante', idAyudante);
    return this.http.get<SesionResponseDTO[]>(`${this.base}/mis-sesiones`, { params });
  }
}
