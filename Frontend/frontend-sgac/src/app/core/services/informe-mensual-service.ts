import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface InformeMensual {
  idInformeMensual?: number;
  idAyudantia: number;
  mes: number;
  anio: number;
  estado: string;
  fechaGeneracion?: string;
  fechaRevisionDocente?: string;
  fechaAprobacionCoordinador?: string;
  observaciones?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InformeMensualService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/informes`;

  generarInforme(idAyudantia: number, mes: number, anio: number): Observable<InformeMensual> {
    return this.http.post<InformeMensual>(`${this.apiUrl}/generar`, null, {
      params: { idAyudantia, mes, anio }
    });
  }

  revisarDocente(idInforme: number, observaciones: string): Observable<InformeMensual> {
    return this.http.post<InformeMensual>(`${this.apiUrl}/revisar-docente/${idInforme}`, observaciones);
  }

  aprobarCoordinador(idInforme: number): Observable<InformeMensual> {
    return this.http.post<InformeMensual>(`${this.apiUrl}/aprobar-coordinador/${idInforme}`, null);
  }
}
