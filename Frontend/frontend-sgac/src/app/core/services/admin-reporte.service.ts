import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminReporteService {
  private http = inject(HttpClient);
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API = `${this.baseUrl}/admin/reportes`;

  getAuditoria(params: any): Observable<any[]> {
    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      if (params[key]) httpParams = httpParams.append(key, params[key]);
    });
    return this.http.get<any[]>(`${this.API}/auditoria`, { params: httpParams });
  }

  getFacultades(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/facultades`);
  }

  getCarreras(idFacultad?: number): Observable<any[]> {
    let params = new HttpParams();
    if (idFacultad) params = params.append('idFacultad', idFacultad);
    return this.http.get<any[]>(`${this.API}/carreras`, { params });
  }

  getAsignaturas(idCarrera?: number): Observable<any[]> {
    let params = new HttpParams();
    if (idCarrera) params = params.append('idCarrera', idCarrera);
    return this.http.get<any[]>(`${this.API}/asignaturas`, { params });
  }

  getConvocatorias(idAsignatura?: number, idPeriodo?: number): Observable<any[]> {
    let params = new HttpParams();
    if (idAsignatura) params = params.append('idAsignatura', idAsignatura);
    if (idPeriodo) params = params.append('idPeriodo', idPeriodo);
    return this.http.get<any[]>(`${this.API}/convocatorias`, { params });
  }

  getPersonal(params: any): Observable<any[]> {
    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      if (params[key]) httpParams = httpParams.append(key, params[key]);
    });
    return this.http.get<any[]>(`${this.API}/personal`, { params: httpParams });
  }

  getPostulantes(params: any): Observable<any[]> {
    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      if (params[key]) httpParams = httpParams.append(key, params[key]);
    });
    return this.http.get<any[]>(`${this.API}/postulantes`, { params: httpParams });
  }

  getUsuarios(params: any): Observable<any[]> {
    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      if (params[key]) httpParams = httpParams.append(key, params[key]);
    });
    return this.http.get<any[]>(`${this.API}/usuarios`, { params: httpParams });
  }
}
