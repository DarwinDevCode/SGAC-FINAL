import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EvaluacionDesempeno {
  idEvaluacionDesempeno?: number;
  idRegistroActividad: number;
  idDocente: number;
  puntaje: number;
  retroalimentacion: string;
  fechaEvaluacion?: string;
}

@Injectable({
  providedIn: 'root'
})
export class EvaluacionDesempenoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/evaluaciones-desempeno`;

  evaluarSesion(idRegistroActividad: number, idDocente: number, puntaje: number, retroalimentacion: string): Observable<EvaluacionDesempeno> {
    return this.http.post<EvaluacionDesempeno>(`${this.apiUrl}/evaluar`, retroalimentacion, {
      params: { idRegistroActividad, idDocente, puntaje }
    });
  }
}
