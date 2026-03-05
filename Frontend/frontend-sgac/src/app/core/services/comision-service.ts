import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ComisionDTO, ComisionRequestDTO } from '../dto/comision';

const API = 'http://localhost:8080/api/comisiones';

@Injectable({ providedIn: 'root' })
export class ComisionService {
  private http = inject(HttpClient);

  listarPorConvocatoria(idConvocatoria: number): Observable<ComisionDTO[]> {
    return this.http.get<ComisionDTO[]>(`${API}/convocatoria/${idConvocatoria}`);
  }

  crear(dto: ComisionRequestDTO): Observable<ComisionDTO> {
    return this.http.post<ComisionDTO>(`${API}/crear`, dto);
  }

  actualizar(id: number, dto: ComisionRequestDTO): Observable<ComisionDTO> {
    return this.http.put<ComisionDTO>(`${API}/actualizar/${id}`, dto);
  }

  desactivar(id: number): Observable<any> {
    return this.http.delete(`${API}/desactivar/${id}`, { responseType: 'text' });
  }
}
