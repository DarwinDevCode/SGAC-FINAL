import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { TipoRequisitoPostulacionResponseDTO, PostulacionResponseDTO, RequisitoAdjuntoResponseDTO } from '../dto/postulacion';
import { EvaluacionMeritosResponseDTO, EvaluacionOposicionResponseDTO } from '../dto/evaluacion';
import { NotificacionResponseDTO } from '../dto/notificacion';

const API_CONVOCATORIAS = 'http://localhost:8080/api/convocatorias';
const API_POSTULACIONES = 'http://localhost:8080/api/postulaciones';
const API_EVALUACIONES = 'http://localhost:8080/api/evaluaciones';
const API_NOTIFICACIONES = 'http://localhost:8080/api/notificaciones';
const API_REQUISITOS = 'http://localhost:8080/api/requisitos-adjuntos';

@Injectable({
    providedIn: 'root'
})
export class PostulanteService {
    private http = inject(HttpClient);

    // Estado reactivo para el sidebar u otros componentes
    public tienePostulacionActiva$ = new BehaviorSubject<boolean>(false);

    // Convocatorias
    listarConvocatoriasActivas(): Observable<ConvocatoriaDTO[]> {
        return this.http.get<ConvocatoriaDTO[]>(`${API_CONVOCATORIAS}/listar-vista`);
    }

    // Postulaciones
    listarTiposRequisitos(): Observable<TipoRequisitoPostulacionResponseDTO[]> {
        return this.http.get<TipoRequisitoPostulacionResponseDTO[]>(`${API_POSTULACIONES}/listar-activos`);
    }

    misPostulaciones(idEstudiante: number): Observable<PostulacionResponseDTO[]> {
        return this.http.get<PostulacionResponseDTO[]>(`${API_POSTULACIONES}/mis-postulaciones/${idEstudiante}`);
    }

    /** Actualiza el estado global de postulación */
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
        return this.http.get<boolean>(`${API_POSTULACIONES}/existe`, {
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

        return this.http.post(`${API_POSTULACIONES}/registrar`, formData, { params, responseType: 'text' });
    }

    // Evaluaciones
    obtenerMeritosPorPostulacion(idPostulacion: number): Observable<EvaluacionMeritosResponseDTO> {
        return this.http.get<EvaluacionMeritosResponseDTO>(`${API_EVALUACIONES}/meritos/postulacion/${idPostulacion}`);
    }

    obtenerOposicionPorPostulacion(idPostulacion: number): Observable<EvaluacionOposicionResponseDTO> {
        return this.http.get<EvaluacionOposicionResponseDTO>(`${API_EVALUACIONES}/oposicion/postulacion/${idPostulacion}`);
    }

    // Notificaciones
    listarNotificaciones(idUsuario: number): Observable<NotificacionResponseDTO[]> {
        return this.http.get<NotificacionResponseDTO[]>(`${API_NOTIFICACIONES}/mis-notificaciones/${idUsuario}`);
    }

    marcarNotificacionLeida(idNotificacion: number): Observable<any> {
        return this.http.put(`${API_NOTIFICACIONES}/marcar-leida/${idNotificacion}`, {});
    }

    // --- Documentos adjuntos (Ítems 2 y 8) ---

    /** Lista los documentos adjuntos de una postulación */
    listarDocumentosPostulacion(idPostulacion: number): Observable<RequisitoAdjuntoResponseDTO[]> {
        return this.http.get<RequisitoAdjuntoResponseDTO[]>(`${API_REQUISITOS}/postulacion/${idPostulacion}`);
    }

    /** URL de visualización/descarga de un documento (inline) */
    urlVisualizarDocumento(idRequisito: number): string {
        return `${API_REQUISITOS}/descargar/${idRequisito}`;
    }

    /** Reemplaza un documento observado por uno nuevo (ítem 8) */
    reemplazarDocumento(idAdjunto: number, archivo: File): Observable<RequisitoAdjuntoResponseDTO> {
        const formData = new FormData();
        formData.append('archivo', archivo);
        return this.http.put<RequisitoAdjuntoResponseDTO>(`${API_REQUISITOS}/reemplazar/${idAdjunto}`, formData);
    }
}
