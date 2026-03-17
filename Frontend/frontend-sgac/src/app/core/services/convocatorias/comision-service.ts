import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {Observable, throwError} from 'rxjs';
import {ComisionDetalleResponse, GenerarComisionesResponse} from '../../models/convocatoria/comision';
import {catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ComisionService {
  private http = inject(HttpClient);
  private readonly base = environment.apiUrl;
  private readonly API  = `${this.base}/comisiones`;

  generarAutomatico(): Observable<GenerarComisionesResponse> {
    return this.http.post<GenerarComisionesResponse>(
      `${this.API}/generar`, {}
    ).pipe(catchError(this.handleError));
  }

  obtenerDetalle(idUsuario: number, rol: string): Observable<ComisionDetalleResponse> {
    const params = new HttpParams()
      .set('idUsuario', idUsuario)
      .set('rol', rol.toUpperCase());
    return this.http.get<ComisionDetalleResponse>(
      `${this.API}/detalle`, { params }
    ).pipe(catchError(this.handleError));
  }

  private handleError(err: any): Observable<never> {
    const msg: string =
      err.error?.mensaje ?? err.error?.message ?? 'Error al comunicarse con el servidor.';
    return throwError(() => new Error(msg));
  }
}
