import {inject, Injectable} from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import { environment } from '../../../environments/environment';
import { PermisoRolDTO } from '../dto/permiso-rol-dto';
import {GestionPermisosRequestDTO} from '../dto/gestion-permisos-request-dto';
import {MensajeResponseDTO} from '../dto/mensaje-response-dto';
import {ElementoBdDTO} from '../dto/elemento-bd-dto';
import {TipoObjetoSeguridadDTO} from '../dto/tipo-objeto-seguridad-dto';
import {PrivilegioDTO} from '../dto/privilegio-dto';
import {GestionPermisosMasivoRequestDTO} from '../dto/gestion-permisos-masivo-request-dto';
import {ResultadoMasivoResponseDTO} from '../dto/resultado-masivo-response-dto';

@Injectable({
  providedIn: 'root'
})
export class PermisoService {
  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private apiUrl = `${this.baseUrl}/permisos`;

  http = inject(HttpClient);

  consultarPermisos(filtros: PermisoRolDTO): Observable<PermisoRolDTO[]> {
    let params = new HttpParams();
    if (filtros.rolBd) params = params.set('rolBd', filtros.rolBd);
    if (filtros.esquema) params = params.set('esquema', filtros.esquema);
    if (filtros.categoria) params = params.set('categoria', filtros.categoria);
    if (filtros.privilegio) params = params.set('privilegio', filtros.privilegio);

    return this.http.get<PermisoRolDTO[]>(`${this.apiUrl}/consultar`, { params });
  }

  gestionarPermiso(request: GestionPermisosRequestDTO): Observable<MensajeResponseDTO> {
    return this.http.post<MensajeResponseDTO>(`${this.apiUrl}/gestionar`, request);
  }

  listarEsquemas(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/esquemas`);
  }

  listarTiposObjeto(): Observable<TipoObjetoSeguridadDTO[]> {
    return this.http.get<TipoObjetoSeguridadDTO[]>(`${this.apiUrl}/tipos-objeto`);
  }

  listarElementos(esquema: string, tipoObjeto: string): Observable<string[]> {
    const params = new HttpParams()
      .set('esquema', esquema)
      .set('tipoObjeto', tipoObjeto);
    return this.http.get<string[]>(`${this.apiUrl}/elementos`, { params });
  }

  listarPrivilegios(idTipoObjeto: number): Observable<PrivilegioDTO[]> {
    return this.http.get<PrivilegioDTO[]>(`${this.apiUrl}/privilegios/${idTipoObjeto}`);
  }

  //gestionarPermisosMasivo(request: GestionPermisosMasivoRequestDTO): Observable<ResultadoMasivoResponseDTO> {
  //  return this.http.post<ResultadoMasivoResponseDTO>(`${this.apiUrl}/gestionar-masivo`, request);
  //}


  gestionarPermisosMasivo(request: GestionPermisosMasivoRequestDTO): Observable<ResultadoMasivoResponseDTO> {
    console.log('Enviando solicitud masiva al backend:', request);
    if (request.permisos)
      console.table(request.permisos);

    return this.http.post<ResultadoMasivoResponseDTO>(`${this.apiUrl}/gestionar-masivo`, request)
      .pipe(
        tap(respuesta => console.log('Respuesta del servidor:', respuesta))
      );
  }
}
