// src/app/core/services/evaluacion-oposicion.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError }         from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  OposicionResponse,
  PuntajeJuradoPayload,
  SorteoPayload,
  TemaOposicion
} from '../../models/evaluaciones/EvaluacionOposicion';

@Injectable({ providedIn: 'root' })
export class EvaluacionOposicionService {
  private http = inject(HttpClient);
  private readonly base = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API  = `${this.base}/oposicion`;

  listarTemas(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/temas`, {
      idConvocatoria, accion: 'LISTAR'
    }).pipe(catchError(this.handleError));
  }

  registrarTemas(idConvocatoria: number, temas: { descripcionTema: string }[]): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/temas`, {
      idConvocatoria, accion: 'REGISTRAR', temas
    }).pipe(catchError(this.handleError));
  }

  limpiarBanco(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/temas`, {
      idConvocatoria, accion: 'LIMPIAR'
    }).pipe(catchError(this.handleError));
  }

  ejecutarSorteo(payload: SorteoPayload): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/sorteo`, payload)
      .pipe(catchError(this.handleError));
  }

  obtenerCronograma(idConvocatoria: number): Observable<OposicionResponse> {
    return this.http.get<OposicionResponse>(`${this.API}/cronograma/${idConvocatoria}`)
      .pipe(catchError(this.handleError));
  }

  iniciarEvaluacion(idEvaluacionOposicion: number): Observable<OposicionResponse> {
    return this.http.patch<OposicionResponse>(`${this.API}/estado`, {
      idEvaluacionOposicion, accion: 'INICIAR'
    }).pipe(catchError(this.handleError));
  }

  marcarNoPresento(idEvaluacionOposicion: number): Observable<OposicionResponse> {
    return this.http.patch<OposicionResponse>(`${this.API}/estado`, {
      idEvaluacionOposicion, accion: 'NO_PRESENTO'
    }).pipe(catchError(this.handleError));
  }

  finalizarEvaluacion(idEvaluacionOposicion: number): Observable<OposicionResponse> {
    return this.http.patch<OposicionResponse>(`${this.API}/estado`, {
      idEvaluacionOposicion, accion: 'FINALIZAR'
    }).pipe(catchError(this.handleError));
  }

  registrarPuntaje(payload: PuntajeJuradoPayload): Observable<OposicionResponse> {
    return this.http.post<OposicionResponse>(`${this.API}/puntaje`, payload)
      .pipe(catchError(this.handleError));
  }

  private handleError(err: any): Observable<never> {
    const msg = err.error?.mensaje ?? err.error?.message ?? 'Error al comunicarse con el servidor.';
    return throwError(() => new Error(msg));
  }
}
