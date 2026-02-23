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
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrl = `${this.baseUrl}/evaluaciones`;

  obtenerMeritosPorPostulacion(idPostulacion: number): Observable<EvaluacionMeritosDTO> {
    return this.http.get<EvaluacionMeritosDTO>(`${this.apiUrl}/meritos/postulacion/${idPostulacion}`);
  }
}
