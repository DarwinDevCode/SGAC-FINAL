import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {environment} from '../../../environments/environment';
import {ConvocatoriaDTO} from '../dto/convocatoria';
import {PeriodoAcademicoDTO} from '../dto/periodo-academico';
import {AsignaturaDTO} from '../dto/asignatura';
import {DocenteDTO} from '../dto/docente';

@Injectable({
  providedIn: 'root',
})
export class ConvocatoriaService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly apiUrl = `${this.baseUrl}/convocatorias`;

  getAll(): Observable<ConvocatoriaDTO[]> {
    return this.http.get<ConvocatoriaDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<ConvocatoriaDTO> {
    return this.http.get<ConvocatoriaDTO>(`${this.apiUrl}/${id}`);
  }

  create(convocatoria: ConvocatoriaDTO): Observable<ConvocatoriaDTO> {
    return this.http.post<ConvocatoriaDTO>(`${this.apiUrl}/crear`, convocatoria);
  }

  update(convocatoria: ConvocatoriaDTO): Observable<ConvocatoriaDTO> {
    return this.http.put<ConvocatoriaDTO>(`${this.apiUrl}/actualizar`, convocatoria);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }


  getPeriodoActivo(): Observable<PeriodoAcademicoDTO[]> {
    return this.http.get<PeriodoAcademicoDTO[]>(`${this.baseUrl}/api/recursos/periodos`)
  }

  getAsignaturas(): Observable<AsignaturaDTO[]> {
    return this.http.get<AsignaturaDTO[]>(`${this.baseUrl}/admin/catalogos/asignaturas`);
  }

  getDocentes(): Observable<DocenteDTO[]> {
    return this.http.get<DocenteDTO[]>(`${this.baseUrl}/recursos/docentes`);
  }
}
