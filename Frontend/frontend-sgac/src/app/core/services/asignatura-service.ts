import { inject, Injectable } from '@angular/core';
import {AsignaturaDTO} from '../dto/asignatura';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AsignaturaService{
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

  private readonly API = `${this.baseUrl}/admin/catalogos`
  private http = inject(HttpClient);

  getAsignaturas(): Observable<AsignaturaDTO[]>{
    return this.http.get<AsignaturaDTO[]>(`${this.API}/asignaturas`);
  }

  postAsignaturas(asignatura: AsignaturaDTO): Observable<AsignaturaDTO> {
    const { idAsignatura, activo, carrera, nombreCarrera, ...requestBody } = asignatura;
    return this.http.post<AsignaturaDTO>(`${this.API}/asignaturas`, requestBody);
  }

  putAsignaturas(id: number, asignatura: AsignaturaDTO): Observable<AsignaturaDTO> {
    const { idAsignatura, activo, carrera, nombreCarrera, ...requestBody } = asignatura;
    return this.http.put<AsignaturaDTO>(`${this.API}/asignaturas/${id}`, requestBody);
  }

  desactivarAsignatura(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/asignaturas/${id}/desactivar`, {});
  }
}
