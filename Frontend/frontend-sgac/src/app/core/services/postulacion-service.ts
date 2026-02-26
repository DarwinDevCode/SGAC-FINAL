import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { PostulacionRequestDTO, PostulacionResponseDTO } from '../dto/postulacion';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PostulacionService {
  private http = inject(HttpClient);
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private apiUrl = `${this.baseUrl}/postulaciones`;

  registrar(request: PostulacionRequestDTO, files: File[], tiposRequisito: number[]): Observable<any> {
    const formData = new FormData();

    formData.append('datos', JSON.stringify(request));

    if (files && files.length > 0) {
      files.forEach(file => {
        formData.append('archivos', file);
      });
    }

    let params = new HttpParams();
    tiposRequisito.forEach(id => {
      params = params.append('tiposRequisito', id.toString());
    });

    return this.http.post(`${this.apiUrl}/registrar`, formData, { params });
  }

  listarPorEstudiante(idEstudiante: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.apiUrl}/mis-postulaciones/${idEstudiante}`);
  }

  listarPorConvocatoria(idConvocatoria: number): Observable<PostulacionResponseDTO[]> {
    return this.http.get<PostulacionResponseDTO[]>(`${this.apiUrl}/convocatoria/${idConvocatoria}`);
  }

  cambiarEstado(idPostulacion: number, estado: string, observacion: string): Observable<any> {
    let params = new HttpParams()
      .set('estado', estado)
      .set('observacion', observacion || '');

    return this.http.put(`${this.apiUrl}/cambiar-estado/${idPostulacion}`, null, { params });
  }

  getRequisitosActivos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/listar-activos`);
  }
}
