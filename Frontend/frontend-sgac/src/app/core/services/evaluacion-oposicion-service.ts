import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { EvaluacionOposicionDTO } from '../dto/evaluacion-oposicion';

export interface AsignarComisionRequest {
  idPostulacion: number;
  idComisionSeleccion: number;
  temaExposicion: string;
  fechaEvaluacion: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
}

@Injectable({
  providedIn: 'root',
})
export class EvaluacionOposicionService {
  private http = inject(HttpClient);
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrl = `${this.baseUrl}/evaluaciones`;

  obtenerOposicionPorPostulacion(idPostulacion: number): Observable<EvaluacionOposicionDTO> {
    return this.http.get<EvaluacionOposicionDTO>(`${this.apiUrl}/postulacion/${idPostulacion}`);
  }

  asignarComision(request: AsignarComisionRequest): Observable<EvaluacionOposicionDTO> {
    return this.http.post<EvaluacionOposicionDTO>(`${this.apiUrl}/asignar`, request);
  }
}
