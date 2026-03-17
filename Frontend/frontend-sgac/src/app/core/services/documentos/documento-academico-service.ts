
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {AuthService} from '../auth-service';
import {
  Facultad,
  Carrera,
  TipoDocumento,
  DocumentoVisor,
  DocumentoCrearRequest,
  DocumentoActualizarRequest,
  DocumentoIdResponse,
  DocumentoEliminadoResponse,
  RespuestaOperacion,
} from '../../models/documentos/documento-academico';

@Injectable({ providedIn: 'root' })
export class DocumentoService {
  private readonly base = environment.apiUrl;
  private http = inject(HttpClient);
  private readonly baseUrl = `${this.base}/documentos`;
  private authService = inject(AuthService);

  obtenerFacultades(): Observable<Facultad[]> {
    return this.http
      .get<RespuestaOperacion<Facultad[]>>(`${this.baseUrl}/facultades`)
      .pipe(
        map(resp => this.validarRespuesta(resp)),
        catchError(err => this.handleError(err))
      );
  }

  obtenerCarreras(idFacultad: number): Observable<Carrera[]> {
    if (!idFacultad || idFacultad <= 0) {
      return throwError(() => new Error('ID de facultad inválido'));
    }
    return this.http
      .get<RespuestaOperacion<Carrera[]>>(
        `${this.baseUrl}/carreras/${idFacultad}`
      )
      .pipe(
        map(resp => this.validarRespuesta(resp)),
        catchError(err => this.handleError(err))
      );
  }

  obtenerTiposDocumento(): Observable<TipoDocumento[]> {
    return this.http
      .get<RespuestaOperacion<TipoDocumento[]>>(`${this.baseUrl}/tipos`)
      .pipe(
        map(resp => this.validarRespuesta(resp)),
        catchError(err => this.handleError(err))
      );
  }

  crearDocumento(req: DocumentoCrearRequest): Observable<DocumentoIdResponse> {
    const formData = new FormData();
    formData.append('archivo', req.archivo);
    formData.append('nombre', req.nombre);
    formData.append('idTipo', String(req.idTipo));
    formData.append('idUsuario', String(req.idUsuario));

    if (req.idFacultad !== null) {
      formData.append('idFacultad', String(req.idFacultad));
    }
    if (req.idCarrera !== null) {
      formData.append('idCarrera', String(req.idCarrera));
    }

    return this.http
      .post<RespuestaOperacion<DocumentoIdResponse>>(
        this.baseUrl,
        formData
      )
      .pipe(
        map(resp => this.validarRespuesta(resp)),
        catchError(err => this.handleError(err))
      );
  }

  listarDocumentos(idUsuario: number, rol: string): Observable<DocumentoVisor[]> {
    const params = new HttpParams()
      .set('idUsuario', String(idUsuario))
      .set('rol', rol);

    return this.http
      .get<RespuestaOperacion<DocumentoVisor[]>>(`${this.baseUrl}/visor`, { params })
      .pipe(
        map(resp => this.validarRespuesta(resp)),
        catchError(err => this.handleError(err))
      );
  }

  actualizarDocumento(req: DocumentoActualizarRequest): Observable<void> {
    return this.http
      .put<RespuestaOperacion<void>>(`${this.baseUrl}`, req)
      .pipe(
        map(resp => {
          this.validarRespuesta(resp);
          return;
        }),
        catchError(err => this.handleError(err))
      );
  }

  eliminarDocumento(idDocumento: number): Observable<DocumentoEliminadoResponse> {
    if (!idDocumento || idDocumento <= 0) {
      return throwError(() => new Error('ID de documento inválido'));
    }

    return this.http
      .delete<RespuestaOperacion<DocumentoEliminadoResponse>>(
        `${this.baseUrl}/${idDocumento}`
      )
      .pipe(
        map(resp => this.validarRespuesta(resp)), // Esto devuelve DocumentoEliminadoResponse
        catchError(err => this.handleError(err))
      );
  }

  private validarRespuesta<T>(resp: RespuestaOperacion<T>): T {
    if (!resp || typeof resp.valido !== 'boolean') {
      throw new Error('Respuesta del servidor inválida o malformada');
    }
    if (!resp.valido) {
      throw new Error(resp.mensaje || 'Operación fallida en el servidor');
    }
    return resp.datos;
  }

  private handleError(error: any): Observable<never> {
    let mensaje = 'Error al comunicarse con el servidor';

    if (error.error) {
      if (error.error.mensaje) {
        mensaje = error.error.mensaje;
      } else if (error.error.message) {
        mensaje = error.error.message;
      } else if (typeof error.error === 'string') {
        mensaje = error.error;
      }
    } else if (error.message) {
      mensaje = error.message;
    }

    console.error('[DocumentoService]', mensaje, error);
    return throwError(() => new Error(mensaje));
  }
}
