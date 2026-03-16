import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DecanoResponseDTO, DecanoEstadisticasDTO, ConvocatoriaReporteDTO, LogAuditoriaDTO, DecanoReporteCarreraDTO, DecanoReporteCoordinadorDTO } from '../dto/decano';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { PostulacionResponseDTO } from '../dto/postulacion';
import { AsignaturaDTO } from '../dto/asignatura';
import { CoordinadorPostulanteReporteDTO } from '../dto/coordinador';

@Injectable({
  providedIn: 'root'
})
export class DecanoService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_DECANOS = `${this.baseUrl}/decanos`;
  private readonly API_CONVOCATORIAS = `${this.baseUrl}/convocatorias`;
  private readonly API_POSTULACIONES = `${this.baseUrl}/postulaciones`;

  // --- Decanos API ---
  listarActivos(): Observable<DecanoResponseDTO[]> {
    return this.http.get<DecanoResponseDTO[]>(`${this.API_DECANOS}`);
  }

  obtenerDecanoPorUsuario(idUsuario: number): Observable<DecanoResponseDTO> {
    return this.http.get<DecanoResponseDTO>(`${this.API_DECANOS}/usuario/${idUsuario}`);
  }

  obtenerEstadisticasPorFacultad(idFacultad: number): Observable<DecanoEstadisticasDTO> {
    return this.http.get<DecanoEstadisticasDTO>(`${this.API_DECANOS}/facultad/${idFacultad}/estadisticas`);
  }

  obtenerReporteConvocatorias(idFacultad: number): Observable<ConvocatoriaReporteDTO[]> {
    return this.http.get<ConvocatoriaReporteDTO[]>(`${this.API_DECANOS}/facultad/${idFacultad}/reportes`);
  }

  obtenerReporteAuditoria(idFacultad: number): Observable<LogAuditoriaDTO[]> {
    return this.http.get<LogAuditoriaDTO[]>(`${this.API_DECANOS}/facultad/${idFacultad}/auditoria`);
  }

  // --- Convocatorias API ---
  listarConvocatoriasActivas(): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(`${this.API_CONVOCATORIAS}/listar-vista`);
  }

  // --- Postulaciones API ---
  listarPostulacionesPorConvocatoria(idConvocatoria: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/convocatoria/${idConvocatoria}`);
  }

  // --- Reportes Avanzados para Decano ---
  obtenerReporteCarreras(idFacultad: number): Observable<DecanoReporteCarreraDTO[]> {
    return this.http.get<DecanoReporteCarreraDTO[]>(`${this.API_DECANOS}/facultad/${idFacultad}/reportes/carreras`);
  }

  obtenerReporteAsignaturas(idFacultad: number): Observable<AsignaturaDTO[]> {
    return this.http.get<AsignaturaDTO[]>(`${this.API_DECANOS}/facultad/${idFacultad}/reportes/asignaturas`);
  }

  obtenerReportePostulantes(idFacultad: number): Observable<CoordinadorPostulanteReporteDTO[]> {
    return this.http.get<CoordinadorPostulanteReporteDTO[]>(`${this.API_DECANOS}/facultad/${idFacultad}/reportes/postulantes`);
  }

  obtenerReporteCoordinadores(idFacultad: number): Observable<DecanoReporteCoordinadorDTO[]> {
    return this.http.get<DecanoReporteCoordinadorDTO[]>(`${this.API_DECANOS}/facultad/${idFacultad}/reportes/coordinadores`);
  }
}
