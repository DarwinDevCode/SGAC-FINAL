import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError }         from 'rxjs/operators';
import { environment }        from '../../../../environments/environment';
import {
  EvaluacionMeritosResponse,
  GuardarMeritosRequest,
  GuardarMeritosResponse,
  ListaPostulacionesMeritosResponse,
} from '../../models/evaluaciones/Evaluacionmeritos';

@Injectable({ providedIn: 'root' })
export class EvaluacionMeritosService {

  private http = inject(HttpClient);
  private readonly API =
    `${(environment as any).apiUrl ?? 'http://localhost:8080/api'}/evaluacion-meritos`;

  listar(): Observable<ListaPostulacionesMeritosResponse> {
    return this.http
      .get<ListaPostulacionesMeritosResponse>(`${this.API}/lista`)
      .pipe(catchError(err => throwError(() =>
        new Error(err.error?.mensaje ?? err.error?.message
          ?? 'Error al cargar la lista.'))));
  }

  obtener(idPostulacion: number): Observable<EvaluacionMeritosResponse> {
    return this.http
      .get<EvaluacionMeritosResponse>(`${this.API}/postulacion/${idPostulacion}`)
      .pipe(catchError(err => throwError(() =>
        new Error(err.error?.mensaje ?? err.error?.message
          ?? 'Error al obtener la evaluación.'))));
  }

  guardar(payload: GuardarMeritosRequest): Observable<GuardarMeritosResponse> {
    return this.http
      .post<GuardarMeritosResponse>(`${this.API}`, payload)
      .pipe(catchError(err => throwError(() =>
        new Error(err.error?.mensaje ?? err.error?.message
          ?? 'Error al guardar la evaluación.'))));
  }

  reabrir(idPostulacion: number): Observable<GuardarMeritosResponse> {
    return this.http
      .patch<GuardarMeritosResponse>(`${this.API}/${idPostulacion}/reabrir`, {})
      .pipe(catchError(err => throwError(() =>
        new Error(err.error?.mensaje ?? err.error?.message
          ?? 'Error al reabrir la evaluación.'))));
  }
}
