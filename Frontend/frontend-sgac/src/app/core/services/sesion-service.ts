import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {SesionListado} from '../dto/sesion-listado';
import {SesionDetalle} from '../dto/sesion-detalle';
import {Evidencia} from '../dto/evidencia';
import {ProgresoGeneral} from '../dto/progreso-general';
import {ControlSemanal} from '../dto/control-semanal';
import {RegistrarSesionResponse} from '../dto/registrar-sesion-response';
import {RegistrarSesionRequest} from '../dto/registrar-sesion-request';
import {FiltrosSesionRequest} from '../dto/filtros-sesion-request';

@Injectable({
  providedIn: 'root'
})
export class SesionService {

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';


  private readonly URL = `${this.baseUrl}/sesiones`;

  constructor(private http: HttpClient) {}

  listarSesiones(
    idUsuario: number,
    filtros?: FiltrosSesionRequest
  ): Observable<SesionListado[]> {

    let params = new HttpParams().set('idUsuario', idUsuario);

    if (filtros?.fechaDesde)  params = params.set('fechaDesde',  filtros.fechaDesde);
    if (filtros?.fechaHasta)  params = params.set('fechaHasta',  filtros.fechaHasta);
    if (filtros?.estado)      params = params.set('estado',      filtros.estado);
    if (filtros?.idPeriodo)   params = params.set('idPeriodo',   filtros.idPeriodo);

    return this.http.get<SesionListado[]>(this.URL, { params });
  }

  detalleSesion(
    idUsuario: number,
    idRegistro: number
  ): Observable<SesionDetalle> {

    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.get<SesionDetalle>(`${this.URL}/${idRegistro}`, { params });
  }

  evidenciasSesion(
    idUsuario: number,
    idRegistro: number
  ): Observable<Evidencia[]> {

    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.get<Evidencia[]>(`${this.URL}/${idRegistro}/evidencias`, { params });
  }

  progresoGeneral(idUsuario: number): Observable<ProgresoGeneral> {
    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.get<ProgresoGeneral>(`${this.URL}/progreso`, { params });
  }

  controlSemanal(idUsuario: number): Observable<ControlSemanal> {
    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.get<ControlSemanal>(`${this.URL}/control-semanal`, { params });
  }

  registrarSesion(
    idUsuario: number,
    request: RegistrarSesionRequest
  ): Observable<RegistrarSesionResponse> {

    const params = new HttpParams().set('idUsuario', idUsuario);
    return this.http.post<RegistrarSesionResponse>(this.URL, request, { params });
  }
}
