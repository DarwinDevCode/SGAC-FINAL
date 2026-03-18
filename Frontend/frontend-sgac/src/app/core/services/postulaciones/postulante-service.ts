import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Observable, BehaviorSubject, catchError, throwError} from 'rxjs';
import { map } from 'rxjs/operators';
import { ConvocatoriaDTO } from '../../dto/convocatoria';
import { TipoRequisitoPostulacionResponseDTO, PostulacionResponseDTO, RequisitoAdjuntoResponseDTO } from '../../dto/postulacion';
import { EvaluacionMeritosResponseDTO, EvaluacionOposicionResponseDTO } from '../../dto/evaluacion';
import { NotificacionResponseDTO } from '../../dto/notificacion';
import { DetallePostulacionResponseDTO, SubsanacionDocumentoResponseDTO } from '../../dto/detalle-postulacion';
import {environment} from '../../../../environments/environment';
import {TribunalEvaluacionResponse} from '../../models/postulaciones/postulacion';
import {RespuestaOperacion} from '../../models/general/respuesta-operacion';

@Injectable({
    providedIn: 'root'
})
export class PostulanteService {
    private http = inject(HttpClient);

    public tienePostulacionActiva$ = new BehaviorSubject<boolean>(false);

  private readonly baseUrl = environment.apiUrl;
  private readonly API_CONVOCATORIAS = `${this.baseUrl}/convocatorias`
  private readonly API_POSTULACIONES = `${this.baseUrl}/postulaciones`
  private readonly API_EVALUACIONES = `${this.baseUrl}/evaluaciones`
  private readonly API_NOTIFICACIONES = `${this.baseUrl}/notificaciones`
  private readonly API_REQUISITOS = `${this.baseUrl}/requisitos-adjuntos`




  listarConvocatoriasActivas(): Observable<ConvocatoriaDTO[]> {
        return this.http.get<ConvocatoriaDTO[]>(`${this.API_CONVOCATORIAS}/listar-vista`);
    }

    listarTiposRequisitos(): Observable<TipoRequisitoPostulacionResponseDTO[]> {
        return this.http.get<TipoRequisitoPostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/listar-activos`);
    }

    misPostulaciones(idEstudiante: number): Observable<PostulacionResponseDTO[]> {
        return this.http.get<PostulacionResponseDTO[]>(`${this.API_POSTULACIONES}/mis-postulaciones/${idEstudiante}`);
    }

    verificarEstadoGlobalPostulacion(idEstudiante: number) {
        this.misPostulaciones(idEstudiante).subscribe({
            next: (postulaciones) => {
                const activas = (postulaciones || []).filter(p => p.estadoPostulacion !== 'RECHAZADO');
                this.tienePostulacionActiva$.next(activas.length > 0);
            },
            error: () => this.tienePostulacionActiva$.next(false)
        });
    }

    /** Verifica si el estudiante ya se postuló a una convocatoria específica */
    existePostulacion(idEstudiante: number, idConvocatoria: number): Observable<boolean> {
        return this.http.get<boolean>(`${this.API_POSTULACIONES}/existe`, {
            params: { idEstudiante: idEstudiante.toString(), idConvocatoria: idConvocatoria.toString() }
        });
    }

    registrarPostulacion(datos: any, archivos: File[], tiposRequisito: number[]): Observable<any> {
        const formData = new FormData();
        formData.append('datos', JSON.stringify(datos));

        archivos.forEach((archivo) => {
            formData.append('archivos', archivo);
        });

        // tiposRequisito va como query param (@RequestParam en Spring)
        let params = new HttpParams();
        tiposRequisito.forEach((idRequisito) => {
            params = params.append('tiposRequisito', idRequisito.toString());
        });

        return this.http.post(`${this.API_POSTULACIONES}/registrar`, formData, { params, responseType: 'text' });
    }

    // Evaluaciones
    obtenerMeritosPorPostulacion(idPostulacion: number): Observable<EvaluacionMeritosResponseDTO> {
        return this.http.get<EvaluacionMeritosResponseDTO>(`${this.API_EVALUACIONES}/meritos/postulacion/${idPostulacion}`);
    }

    obtenerOposicionPorPostulacion(idPostulacion: number): Observable<EvaluacionOposicionResponseDTO> {
        return this.http.get<EvaluacionOposicionResponseDTO>(`${this.API_EVALUACIONES}/oposicion/postulacion/${idPostulacion}`);
    }

    // Notificaciones
    listarNotificaciones(idUsuario: number): Observable<NotificacionResponseDTO[]> {
        return this.http.get<NotificacionResponseDTO[]>(`${this.API_NOTIFICACIONES}/ultimas`).pipe(
            catchError(() => this.http.get<NotificacionResponseDTO[]>(`${this.API_NOTIFICACIONES}/mis-notificaciones/${idUsuario}`))
        );
    }

    marcarNotificacionLeida(idNotificacion: number): Observable<any> {
        // Endpoint nuevo recomendado: /api/notificaciones/{id}/leida
        return this.http.put(`${this.API_NOTIFICACIONES}/${idNotificacion}/leida`, {}).pipe(
            // Fallback endpoint anterior
            catchError(() => this.http.put(`${this.API_NOTIFICACIONES}/marcar-leida/${idNotificacion}`, {}))
        );
    }

    // --- Documentos adjuntos (Ítems 2 y 8) ---

    listarDocumentosPostulacion(idPostulacion: number): Observable<RequisitoAdjuntoResponseDTO[]> {
        return this.http.get<RequisitoAdjuntoResponseDTO[]>(`${this.API_REQUISITOS}/postulacion/${idPostulacion}`);
    }

    urlVisualizarDocumento(idRequisito: number): string {
        return `${this.API_REQUISITOS}/descargar/${idRequisito}`;
    }

    reemplazarDocumento(idAdjunto: number, archivo: File): Observable<RequisitoAdjuntoResponseDTO> {
        const formData = new FormData();
        formData.append('archivo', archivo);
        return this.http.put<RequisitoAdjuntoResponseDTO>(`${this.API_REQUISITOS}/reemplazar/${idAdjunto}`, formData);
    }

    obtenerMiPostulacionActiva(idUsuario: number): Observable<DetallePostulacionResponseDTO> {
        return this.http.get<DetallePostulacionResponseDTO>(`${this.API_POSTULACIONES}/mi-postulacion/${idUsuario}`);
    }

    subsanarDocumentoObservado(idUsuario: number, idRequisitoAdjunto: number, archivo: File): Observable<SubsanacionDocumentoResponseDTO> {
        const formData = new FormData();
        formData.append('archivo', archivo);
        return this.http.put<SubsanacionDocumentoResponseDTO>(
            `${this.API_REQUISITOS}/subsanar/${idUsuario}/${idRequisitoAdjunto}`,
            formData
        );
    }

  obtenerTribunalEvaluacion(idUsuario: number): Observable<TribunalEvaluacionResponse> {
    if (!idUsuario || idUsuario <= 0)
      return throwError(() => new Error('ID de usuario inválido'));

    return this.http
      .get<RespuestaOperacion<TribunalEvaluacionResponse>>(
        `${this.API_POSTULACIONES}/tribunal/${idUsuario}`
      )
      .pipe(
        map(resp => this.validarRespuesta(resp)),
        catchError(err => this.handleError(err))
      );
  }

  private handleError(error: any): Observable<never> {
    let mensaje = 'Error al comunicarse con el servidor';

    if (error.error) {
      if (error.error.mensaje)
        mensaje = error.error.mensaje;
      else if (error.error.message)
        mensaje = error.error.message;
      else if (typeof error.error === 'string')
        mensaje = error.error;
    } else if (error.message)
      mensaje = error.message;


    console.error('[PostulacionService]', mensaje, error);
    return throwError(() => new Error(mensaje));
  }

  private validarRespuesta<T>(resp: RespuestaOperacion<T>): T {
    if (!resp || typeof resp.valido !== 'boolean')
      throw new Error('Respuesta del servidor inválida o malformada');
    if (!resp.valido)
      throw new Error(resp.mensaje || 'Operación fallida en el servidor');
    return resp.datos;
  }
}
