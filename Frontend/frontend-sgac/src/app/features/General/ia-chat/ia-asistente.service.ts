import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

export interface IaRequest {
  tipoConsulta: 'REGLAMENTO' | 'REVISION_TEXTO' | 'SUGERENCIA_RUBRICA';
  prompt: string;
}

export interface IaResponse {
  respuesta: string;
  tokensUsados: number;
}

@Injectable({
  providedIn: 'root'
})
export class IaAsistenteService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/ia-asistente`;

  consultar(request: IaRequest): Observable<IaResponse> {
    return this.http.post<IaResponse>(`${this.apiUrl}/consultar`, request);
  }
}
