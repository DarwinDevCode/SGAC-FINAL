import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PostulacionListadoCoordinador,
  DetallePostulacionCoordinador,
  EvaluarDocumentoRequest,
  EvaluacionDocumentoResponse,
  DictaminarPostulacionRequest,
  DictamenPostulacionResponse,
  CambioEstadoRevisionResponse,
  AsignarComisionRequest
} from '../dto/evaluacion-postulacion';

/**
 * Servicio para la evaluación de postulaciones por parte del coordinador
 */
@Injectable({
  providedIn: 'root'
})
export class EvaluacionPostulacionService {
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

  private apiUrl = `${this.baseUrl}/coordinador/evaluacion`;

  constructor(private http: HttpClient) { }

  /**
   * Lista todas las postulaciones de la carrera del coordinador
   */
  listarPostulaciones(idUsuario: number): Observable<PostulacionListadoCoordinador[]> {
    return this.http.get<PostulacionListadoCoordinador[]>(
      `${this.apiUrl}/postulaciones/${idUsuario}`
    );
  }

  /**
   * Obtiene el detalle completo de una postulación
   */
  obtenerDetallePostulacion(idUsuario: number, idPostulacion: number): Observable<DetallePostulacionCoordinador> {
    return this.http.get<DetallePostulacionCoordinador>(
      `${this.apiUrl}/postulaciones/${idUsuario}/detalle/${idPostulacion}`
    );
  }

  /**
   * Inicia la revisión de una postulación (cambia estado a EN_REVISION)
   */
  iniciarRevision(idUsuario: number, idPostulacion: number): Observable<CambioEstadoRevisionResponse> {
    return this.http.post<CambioEstadoRevisionResponse>(
      `${this.apiUrl}/postulaciones/${idUsuario}/iniciar-revision/${idPostulacion}`,
      {}
    );
  }

  /**
   * Evalúa un documento individual (VALIDAR, OBSERVAR, RECHAZAR)
   */
  evaluarDocumento(idUsuario: number, request: EvaluarDocumentoRequest): Observable<EvaluacionDocumentoResponse> {
    return this.http.post<EvaluacionDocumentoResponse>(
      `${this.apiUrl}/documentos/${idUsuario}/evaluar`,
      request
    );
  }

  /**
   * Dictamina (aprueba o rechaza) una postulación completa
   */
  dictaminarPostulacion(idUsuario: number, request: DictaminarPostulacionRequest): Observable<DictamenPostulacionResponse> {
    return this.http.post<DictamenPostulacionResponse>(
      `${this.apiUrl}/postulaciones/${idUsuario}/dictaminar`,
      request
    );
  }

  /**
   * Obtiene la URL para descargar un documento
   */
  getUrlDescargaDocumento(idUsuario: number, idRequisitoAdjunto: number): string {
    return `${this.apiUrl}/documentos/${idUsuario}/descargar/${idRequisitoAdjunto}`;
  }

  /**
   * Obtiene la URL para visualizar un documento (inline)
   */
  getUrlVisualizarDocumento(idUsuario: number, idRequisitoAdjunto: number): string {
    return `${this.apiUrl}/documentos/${idUsuario}/visualizar/${idRequisitoAdjunto}`;
  }

  /**
   * Descarga un documento como blob
   */
  descargarDocumento(idUsuario: number, idRequisitoAdjunto: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/documentos/${idUsuario}/descargar/${idRequisitoAdjunto}`,
      { responseType: 'blob' }
    );
  }

  /**
   * Visualiza un documento como blob
   */
  visualizarDocumento(idUsuario: number, idRequisitoAdjunto: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/documentos/${idUsuario}/visualizar/${idRequisitoAdjunto}`,
      { responseType: 'blob' }
    );
  }

  /**
   * Asigna una comisión evaluadora a una postulación aprobada
   */
  asignarComision(request: AsignarComisionRequest): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/evaluaciones/asignar`,
      request
    );
  }
}

