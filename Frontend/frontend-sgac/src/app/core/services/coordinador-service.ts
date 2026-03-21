import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CoordinadorResponseDTO, CoordinadorEstadisticasDTO, CoordinadorConvocatoriaReporteDTO, CoordinadorPostulanteReporteDTO } from '../dto/coordinador';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { PostulacionResponseDTO, RequisitoAdjuntoResponseDTO } from '../dto/postulacion';

export interface ComisionSeleccionDTO {
  idComisionSeleccion?: number;
  idConvocatoria: number;
  nombreComision: string;
  fechaConformacion?: string;
  activo?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CoordinadorService {
  private http = inject(HttpClient);

  private readonly baseUrl = environment.apiUrl;
  private readonly API_COORDINADORES = `${this.baseUrl}/coordinadores`;
  private readonly API_CONVOCATORIAS = `${this.baseUrl}/convocatorias`;
  private readonly API_POSTULACIONES = `${this.baseUrl}/postulaciones`;
  private readonly API_COMISIONES = `${this.baseUrl}/comisiones`;

  listarActivos(): Observable<CoordinadorResponseDTO[]> {
    return this.http.get<CoordinadorResponseDTO[]>(`${this.API_COORDINADORES}`);
  }

  obtenerCoordinadorPorUsuario(idUsuario: number): Observable<CoordinadorResponseDTO> {
    return this.http.get<CoordinadorResponseDTO>(`${this.API_COORDINADORES}/usuario/${idUsuario}`);
  }

  obtenerEstadisticasPropias(idUsuario: number): Observable<CoordinadorEstadisticasDTO> {
    return this.http.get<CoordinadorEstadisticasDTO>(`${this.API_COORDINADORES}/me/${idUsuario}/estadisticas`);
  }

  obtenerReporteConvocatoriasPropias(idUsuario: number): Observable<CoordinadorConvocatoriaReporteDTO[]> {
    return this.http.get<CoordinadorConvocatoriaReporteDTO[]>(`${this.API_COORDINADORES}/me/${idUsuario}/reportes/convocatorias`);
  }

  obtenerReportePostulantesPropios(idUsuario: number): Observable<CoordinadorPostulanteReporteDTO[]> {
    return this.http.get<CoordinadorPostulanteReporteDTO[]>(`${this.API_COORDINADORES}/me/${idUsuario}/reportes/postulantes`);
  }

  listarConvocatoriasPorCarrera(idCarrera: number): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(`${this.API_CONVOCATORIAS}/listar-vista`);
  }

  listarPostulacionesPorConvocatoria(idConvocatoria: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/convocatoria/${idConvocatoria}`);
  }

  listarPostulacionesPorCarrera(idCarrera: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/carrera/${idCarrera}`);
  }

  listarPendientesPorCarrera(idCarrera: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/pendientes/carrera/${idCarrera}`);
  }

  listarEnEvaluacionPorCarrera(idCarrera: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/en-evaluacion/carrera/${idCarrera}`);
  }

  cambiarEstadoPostulacion(idPostulacion: number, estado: string, observacion: string, idCoordinador?: number): Observable<any> {
    const params: any = { estado, observacion };
    if (idCoordinador) params['idCoordinador'] = idCoordinador;
    return this.http.put(`${this.API_POSTULACIONES}/cambiar-estado/${idPostulacion}`,
      null, { params, responseType: 'text' });
  }

  // Comisión de selección
  crearComision(datos: ComisionSeleccionDTO): Observable<ComisionSeleccionDTO> {
    return this.http.post<ComisionSeleccionDTO>(`${this.API_COMISIONES}/crear`, datos);
  }

  listarComisionesPorConvocatoria(idConvocatoria: number): Observable<ComisionSeleccionDTO[]> {
    return this.http.get<ComisionSeleccionDTO[]>(`${this.API_COMISIONES}/convocatoria/${idConvocatoria}`);
  }

  // Requisitos adjuntos (documentos del postulante) — Ítem 2
  listarDocumentosPorPostulacion(idPostulacion: number): Observable<RequisitoAdjuntoResponseDTO[]> {
    return this.http.get<RequisitoAdjuntoResponseDTO[]>(`${this.baseUrl}/requisitos-adjuntos/postulacion/${idPostulacion}`);
  }

  getUrlDescargaDocumento(idRequisito: number): string {
    return `${this.baseUrl}/requisitos-adjuntos/descargar/${idRequisito}`;
  }

  // --- Ítem 5: Importar requisitos entre perídos ---

  /** Importa los requisitos activos de un período origen al destino */
  importarRequisitosDePeriodo(idOrigen: number, idDestino: number): Observable<{ mensaje: string; cantidadImportada: number }> {
    return this.http.post<any>(
      `${this.baseUrl}/periodos-requisitos/importar`,
      null,
      { params: { idOrigen: idOrigen.toString(), idDestino: idDestino.toString() } }
    );
  }

  obtenerDetalleAyudantiaCompleto(idAyudantia: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/ayudantia-detalle/${idAyudantia}/completo`);
  }
}
