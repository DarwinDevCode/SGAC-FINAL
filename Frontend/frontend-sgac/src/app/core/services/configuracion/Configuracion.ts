import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  StandardResponse,
  PeriodoAcademicoRequest,
  PeriodoFaseResponse,
  AjusteCronogramaRequest
} from '../../models/configuracion/Configuracion';
import {CronogramaActivoResponse, PeriodoInfo, FaseInfo} from '../../models/Cronograma';

@Injectable({
  providedIn: 'root'
})
export class ConfiguracionService {

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  constructor(private http: HttpClient) {}

  abrirPeriodo(data: PeriodoAcademicoRequest): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(
      `${this.baseUrl}/admin/configuracion/periodos/abrir`,
      data
    );
  }

  obtenerCronograma(idPeriodo: number): Observable<StandardResponse<PeriodoFaseResponse[]>> {
    return this.http.get<StandardResponse<PeriodoFaseResponse[]>>(
      `${this.baseUrl}/admin/configuracion/cronograma/${idPeriodo}`
    );
  }

  guardarCronograma(data: AjusteCronogramaRequest): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(
      `${this.baseUrl}/admin/configuracion/cronograma/guardar`,
      data
    );
  }

  iniciarPeriodo(idPeriodo: number): Observable<StandardResponse<number>> {
    return this.http.post<StandardResponse<number>>(
      `${this.baseUrl}/admin/configuracion/periodos/${idPeriodo}/iniciar`, {}
    );
  }

  obtenerCronogramaActual(): Observable<CronogramaActivoResponse> {
    return this.http.get<CronogramaActivoResponse>(`${this.baseUrl}/admin/configuracion/actual`);
  }
}
