import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TipoRol {
  idTipoRol: number;
  nombreTipoRol: string;
  activo: boolean;
}

export interface Usuario {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  correo: string;
  cedula: string;
  nombreUsuario: string;
  roles: TipoRol[];
  activo: boolean;
}

export interface UsuarioRequest {
  idUsuario?: number;
  nombres: string;
  apellidos: string;
  cedula: string;
  correo: string;
  nombreUsuario: string;
  password?: string;
  roles: string[];
  idCarrera?: number;
  idFacultad?: number;
  semestre?: number;
  matricula?: string;
  horasAyudante?: number;
}

export interface Facultad { idFacultad: number; nombreFacultad: string; }
export interface Carrera { idCarrera: number; idFacultad: number; nombreFacultad: string; nombreCarrera: string; }
export interface Docente { idDocente: number; nombres: string; apellidos: string; cedula: string; }
export interface Asignatura { idAsignatura: number; nombreAsignatura: string; semestre: number; idCarrera: number; }
export interface PeriodoAcademico { idPeriodo: number; nombrePeriodo: string; activo: boolean; }

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

  private readonly API_AUTH = `${this.baseUrl}/auth`;
  private readonly API_RECURSOS = `${this.baseUrl}/recursos`;

  listarTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.API_AUTH);
  }

  crear(usuario: UsuarioRequest): Observable<any> {
    const rol = usuario.roles[0];
    let endpoint = '';

    const finalPayload: any = {
      nombres: usuario.nombres,
      apellidos: usuario.apellidos,
      cedula: usuario.cedula,
      correo: usuario.correo,
      username: usuario.nombreUsuario,
      password: usuario.password
    };

    switch (rol) {
      case 'ESTUDIANTE':
        endpoint = '/registro-estudiante';
        Object.assign(finalPayload, {
          idCarrera: usuario.idCarrera,
          matricula: usuario.matricula,
          semestre: usuario.semestre
        });
        break;
      case 'DOCENTE':
        endpoint = '/registro-docente';
        break;
      case 'COORDINADOR':
        endpoint = '/registro-coordinador';
        finalPayload.idCarrera = usuario.idCarrera;
        break;
      case 'DECANO':
        endpoint = '/registro-decano';
        finalPayload.idFacultad = usuario.idFacultad;
        break;
      case 'ADMINISTRADOR':
        endpoint = '/registro-admin';
        break;
      case 'AYUDANTE_CATEDRA':
        endpoint = '/registro-ayudante-directo';
        finalPayload.horasAyudante = usuario.horasAyudante;
        break;
      default:
        throw new Error(`Rol no soportado: ${rol}`);
    }

    return this.http.post(`${this.API_AUTH}${endpoint}`, finalPayload);
  }

  cambiarEstado(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_AUTH}/${id}/estado`, {});
  }

  cambiarEstadoRol(idUsuario: number, idTipoRol: number): Observable<void> {
    return this.http.patch<void>(`${this.API_AUTH}/${idUsuario}/roles/${idTipoRol}/estado`, {});
  }

  listarFacultades(): Observable<Facultad[]> {
    return this.http.get<Facultad[]>(`${this.API_RECURSOS}/facultades`);
  }

  listarCarreras(): Observable<Carrera[]> {
    return this.http.get<Carrera[]>(`${this.API_RECURSOS}/carreras`);
  }

  listarDocentes(): Observable<Docente[]> {
    return this.http.get<Docente[]>(`${this.API_RECURSOS}/docentes`);
  }

  listarAsignaturas(): Observable<Asignatura[]> {
    return this.http.get<Asignatura[]>(`${this.API_RECURSOS}/asignaturas`);
  }

  obtenerPeriodoActivo(): Observable<PeriodoAcademico> {
    return this.http.get<PeriodoAcademico>(`${this.API_RECURSOS}/periodos`);
  }
}
