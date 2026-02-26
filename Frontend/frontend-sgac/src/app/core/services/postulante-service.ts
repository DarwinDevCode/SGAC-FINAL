import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConvocatoriaDTO } from '../dto/convocatoria';
import { TipoRequisitoPostulacionResponseDTO, PostulacionResponseDTO } from '../dto/postulacion';
import { EvaluacionMeritosResponseDTO, EvaluacionOposicionResponseDTO } from '../dto/evaluacion';
import { NotificacionResponseDTO } from '../dto/notificacion';

const API_CONVOCATORIAS = 'http://localhost:8080/api/convocatorias';
const API_POSTULACIONES = 'http://localhost:8080/api/postulaciones';
const API_EVALUACIONES = 'http://localhost:8080/api/evaluaciones';
const API_NOTIFICACIONES = 'http://localhost:8080/api/notificaciones';

@Injectable({
    providedIn: 'root'
})
export class PostulanteService {
    private http = inject(HttpClient);

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
}
