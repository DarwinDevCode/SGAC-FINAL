import {inject, Injectable} from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PermisoRolDTO } from '../dto/permiso-rol-dto';
import {GestionPermisosRequestDTO} from '../dto/gestion-permisos-request-dto';
import {MensajeResponseDTO} from '../dto/mensaje-response-dto';

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
}
