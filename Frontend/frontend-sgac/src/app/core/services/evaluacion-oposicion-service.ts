import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {EvaluacionOposicionDTO} from '../dto/evaluacion-oposicion';

@Injectable({
  providedIn: 'root',
})
export class EvaluacionOposicionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/evaluaciones`;

  obtenerOposicionPorPostulacion(idPostulacion: number): Observable<EvaluacionOposicionDTO> {
    return this.http.get<EvaluacionOposicionDTO>(`${this.apiUrl}/oposicion/postulacion/${idPostulacion}`);
  }
}
