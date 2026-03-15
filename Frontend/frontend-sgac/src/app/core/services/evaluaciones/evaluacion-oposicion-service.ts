// src/app/core/services/evaluaciones/evaluacion-oposicion-service.ts
import { Injectable, inject }         from '@angular/core';
import { HttpClient }                 from '@angular/common/http';
import { Observable, throwError }     from 'rxjs';
import { catchError }                 from 'rxjs/operators';
import { environment }                from '../../../../environments/environment';
import {
  ConvocatoriasAptasResponse,
  OposicionResponse,
  PuntajeJuradoPayload,
  SorteoPayload
} from '../../models/evaluaciones/EvaluacionOposicion';
import {AuthService} from '../auth-service';

/** Payload extendido que incluye idConvocatoria para el broadcast WS del backend */
export interface RegistrarPuntajePayload extends PuntajeJuradoPayload {
  idConvocatoria?: number;
}

@Injectable({ providedIn: 'root' })
export class EvaluacionOposicionService {
  private http = inject(HttpClient);
  private readonly base = (environment as any).apiUrl ?? 'http://localhost:8080/api';
  private readonly API  = `${this.base}/oposicion`;
  private authService = inject(AuthService);


  listarTemas(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/temas`,
      { idConvocatoria, accion: 'LISTAR' }
    ).pipe(catchError(this.handleError));
  }

  registrarTemas(idConvocatoria: number, temas: { descripcionTema: string }[]): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/temas`,
      { idConvocatoria, accion: 'REGISTRAR', temas }
    ).pipe(catchError(this.handleError));
  }

  limpiarBanco(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/temas`,
      { idConvocatoria, accion: 'LIMPIAR' }
    ).pipe(catchError(this.handleError));
  }

  ejecutarSorteo(payload: SorteoPayload): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/sorteo`, payload)
      .pipe(catchError(this.handleError));
  }

  obtenerCronograma(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.get<OposicionResponse>(`${this.API}/cronograma/${idConvocatoria}`)
      .pipe(catchError(this.handleError));
  }

  obtenerMiTurno(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.get<OposicionResponse>(`${this.API}/mi-turno/${idConvocatoria}`)
      .pipe(catchError(this.handleError));
  }

  iniciarEvaluacion(idEvaluacionOposicion: number, idConvocatoria?: number): Observable<OposicionResponse> {
    return this.http.patch<OposicionResponse>(`${this.API}/estado`,
      { idEvaluacionOposicion, accion: 'INICIAR', idConvocatoria }
    ).pipe(catchError(this.handleError));
  }

  marcarNoPresento(idEvaluacionOposicion: number, idConvocatoria?: number): Observable<OposicionResponse> {
    return this.http.patch<OposicionResponse>(`${this.API}/estado`,
      { idEvaluacionOposicion, accion: 'NO_PRESENTO', idConvocatoria }
    ).pipe(catchError(this.handleError));
  }

  finalizarEvaluacion(idEvaluacionOposicion: number, idConvocatoria?: number): Observable<OposicionResponse> {
    return this.http.patch<OposicionResponse>(`${this.API}/estado`,
      { idEvaluacionOposicion, accion: 'FINALIZAR', idConvocatoria }
    ).pipe(catchError(this.handleError));
  }

  registrarPuntaje(payload: RegistrarPuntajePayload): Observable<OposicionResponse> {
    const idUsuario = this.authService.getUser()?.idUsuario;
    const body = { ...payload, idUsuario: idUsuario };
    return this.http.post<OposicionResponse>(`${this.API}/puntaje`, body)
      .pipe(catchError(this.handleError));
  }

  listarConvocatoriasAptas(): Observable<ConvocatoriasAptasResponse> {
    return this.http.get<ConvocatoriasAptasResponse>(`${this.API}/convocatorias-aptas`)
      .pipe(catchError(this.handleError));
  }

  private handleError(err: any): Observable<never> {
    const msg = err.error?.mensaje ?? err.error?.message ?? 'Error al comunicarse con el servidor.';
    return throwError(() => new Error(msg));
  }

  resolverMiSala(): Observable<{ exito: boolean; idConvocatoria?: number; nombreAsignatura?: string; nombreCarrera?: string; rolIntegrante?: string; mensaje?: string; prioridad?: number }> {
    return this.http
      .get<any>(`${this.API}/mi-sala`)
      .pipe(catchError(this.handleError));
  }
}
