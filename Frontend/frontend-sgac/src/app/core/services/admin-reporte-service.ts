import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UsuarioGlobalDTO {
  usuario: string;
  email: string;
  roles: string;
  estado: string;
}

export interface PersonalGlobalDTO {
  nombre: string;
  cargoContexto: string;
  estado: string;
}

export interface PostulanteGlobalDTO {
  estudiante: string;
  cedula: string;
  asignatura: string;
  periodo: string;
  estado: string;
}

export interface AyudanteGlobalDTO {
  estudiante: string;
  asignatura: string;
  docente: string;
  horas: number;
  estado: string;
}

export interface AuditoriaGlobalDTO {
  idLog: number;
  fecha: string;
  usuario: string;
  roles: string;
  facultad: string;
  carrera: string;
  accion: string;
  modulo: string;
  detalle: string;
}

@Injectable({ providedIn: 'root' })
export class AdminReporteService {
  private http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/reportes`;

  getUsuarios(): Observable<UsuarioGlobalDTO[]> {
    return this.http.get<UsuarioGlobalDTO[]>(`${this.baseUrl}/usuarios`);
  }

  getPersonal(): Observable<PersonalGlobalDTO[]> {
    return this.http.get<PersonalGlobalDTO[]>(`${this.baseUrl}/personal`);
  }

  getPostulantes(): Observable<PostulanteGlobalDTO[]> {
    return this.http.get<PostulanteGlobalDTO[]>(`${this.baseUrl}/postulantes`);
  }

  getAyudantes(): Observable<AyudanteGlobalDTO[]> {
    return this.http.get<AyudanteGlobalDTO[]>(`${this.baseUrl}/ayudantes`);
  }

  getAuditoria(): Observable<AuditoriaGlobalDTO[]> {
    return this.http.get<AuditoriaGlobalDTO[]>(`${this.baseUrl}/auditoria`);
  }
}
