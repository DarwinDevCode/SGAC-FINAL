import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {EvaluacionMeritosDTO} from '../dto/evaluacion-meritos';

@Injectable({
  providedIn: 'root',
})
export class EvaluacionMeritosService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/evaluaciones`;

  obtenerMeritosPorPostulacion(idPostulacion: number): Observable<EvaluacionMeritosDTO> {
    return this.http.get<EvaluacionMeritosDTO>(`${this.apiUrl}/meritos/postulacion/${idPostulacion}`);
  }
}
