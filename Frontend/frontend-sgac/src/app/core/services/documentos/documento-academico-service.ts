import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ConvocatoriaActivaResponse,
  DocumentoResponse,
  TipoDocumentoResponse,
} from '../../models/documentos/documento-academico';

@Injectable({ providedIn: 'root' })
export class DocumentoAcademicoService {

  private http = inject(HttpClient);
  private base = `${(environment as any).apiUrl ?? 'http://localhost:8080/api'}/documentos-academicos`;

  listarTipos(): Observable<TipoDocumentoResponse[]> {
    return this.http.get<TipoDocumentoResponse[]>(`${this.base}/tipos`);
  }

  listarConvocatoriasActivas(): Observable<ConvocatoriaActivaResponse[]> {
    return this.http.get<ConvocatoriaActivaResponse[]>(
      `${this.base}/convocatorias-activas`
    );
  }

  listarVisor(idConvocatoria?: number | null): Observable<DocumentoResponse[]> {
    let params = new HttpParams();
    if (idConvocatoria != null) {
      params = params.set('idConvocatoria', idConvocatoria);
    }
    return this.http.get<DocumentoResponse[]>(`${this.base}/visor`, { params });
  }

  subir(
    file:           File,
    nombre:         string,
    idTipo:         number,
    idConvocatoria?: number | null
  ): Observable<DocumentoResponse> {
    const fd = new FormData();
    fd.append('file',   file);
    fd.append('nombre', nombre);
    fd.append('idTipo', String(idTipo));
    if (idConvocatoria != null) {
      fd.append('idConvocatoria', String(idConvocatoria));
    }
    return this.http.post<DocumentoResponse>(`${this.base}/subir`, fd);
  }

  actualizar(
    id:     number,
    nombre: string,
    idTipo: number,
    file?:  File | null
  ): Observable<DocumentoResponse> {
    const fd = new FormData();
    fd.append('nombre', nombre);
    fd.append('idTipo', String(idTipo));
    if (file) fd.append('file', file);
    return this.http.put<DocumentoResponse>(`${this.base}/${id}`, fd);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
