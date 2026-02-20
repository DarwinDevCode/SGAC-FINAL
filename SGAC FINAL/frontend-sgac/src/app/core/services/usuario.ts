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

export interface Facultad { idFacultad: number; nombreFacultad: string; activo?: boolean; }
export interface Carrera { idCarrera: number; idFacultad: number; nombreFacultad: string; nombreCarrera: string; activo?: boolean; }
export interface Docente { idDocente: number; nombres: string; apellidos: string; cedula: string; }
export interface Asignatura { idAsignatura: number; nombreAsignatura: string; semestre: number; idCarrera: number; }
export interface PeriodoAcademico { idPeriodo: number; nombrePeriodo: string; activo: boolean; }

export interface FacultadCatalogoRequest { nombreFacultad: string; }
export interface CarreraCatalogoRequest { idFacultad: number; nombreCarrera: string; }
export interface AsignaturaCatalogoRequest { idCarrera: number; nombreAsignatura: string; semestre: number; }
export interface PeriodoCatalogoRequest {
  nombrePeriodo: string;
  fechaInicio: string;
  fechaFin: string;
  estado: string;
}

export interface AsignaturaCatalogo {
  idAsignatura: number;
  idCarrera: number;
  nombreCarrera: string;
  nombreAsignatura: string;
  semestre: number;
  activo?: boolean;
}

export interface PeriodoCatalogo {
  idPeriodoAcademico: number;
  nombrePeriodo: string;
  fechaInicio: string;
  fechaFin: string;
  estado: string;
  activo?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

  private readonly API_AUTH = `${this.baseUrl}/auth`;
  private readonly API_RECURSOS = `${this.baseUrl}/recursos`;
  private readonly API_ADMIN_CATALOGOS = `${this.baseUrl}/admin/catalogos`;

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

  // CRUD catalogos administrables
  listarFacultadesCatalogo(): Observable<Facultad[]> {
    return this.http.get<Facultad[]>(`${this.API_ADMIN_CATALOGOS}/facultades`);
  }

  crearFacultadCatalogo(payload: FacultadCatalogoRequest): Observable<Facultad> {
    return this.http.post<Facultad>(`${this.API_ADMIN_CATALOGOS}/facultades`, payload);
  }

  actualizarFacultadCatalogo(id: number, payload: FacultadCatalogoRequest): Observable<Facultad> {
    return this.http.put<Facultad>(`${this.API_ADMIN_CATALOGOS}/facultades/${id}`, payload);
  }

  desactivarFacultadCatalogo(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_ADMIN_CATALOGOS}/facultades/${id}/desactivar`, {});
  }

  listarCarrerasCatalogo(): Observable<Carrera[]> {
    return this.http.get<Carrera[]>(`${this.API_ADMIN_CATALOGOS}/carreras`);
  }

  crearCarreraCatalogo(payload: CarreraCatalogoRequest): Observable<Carrera> {
    return this.http.post<Carrera>(`${this.API_ADMIN_CATALOGOS}/carreras`, payload);
  }

  actualizarCarreraCatalogo(id: number, payload: CarreraCatalogoRequest): Observable<Carrera> {
    return this.http.put<Carrera>(`${this.API_ADMIN_CATALOGOS}/carreras/${id}`, payload);
  }

  desactivarCarreraCatalogo(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_ADMIN_CATALOGOS}/carreras/${id}/desactivar`, {});
  }

  listarAsignaturasCatalogo(): Observable<AsignaturaCatalogo[]> {
    return this.http.get<AsignaturaCatalogo[]>(`${this.API_ADMIN_CATALOGOS}/asignaturas`);
  }

  crearAsignaturaCatalogo(payload: AsignaturaCatalogoRequest): Observable<AsignaturaCatalogo> {
    return this.http.post<AsignaturaCatalogo>(`${this.API_ADMIN_CATALOGOS}/asignaturas`, payload);
  }

  actualizarAsignaturaCatalogo(id: number, payload: AsignaturaCatalogoRequest): Observable<AsignaturaCatalogo> {
    return this.http.put<AsignaturaCatalogo>(`${this.API_ADMIN_CATALOGOS}/asignaturas/${id}`, payload);
  }

  desactivarAsignaturaCatalogo(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_ADMIN_CATALOGOS}/asignaturas/${id}/desactivar`, {});
  }

  listarPeriodosCatalogo(): Observable<PeriodoCatalogo[]> {
    return this.http.get<PeriodoCatalogo[]>(`${this.API_ADMIN_CATALOGOS}/periodos`);
  }

  crearPeriodoCatalogo(payload: PeriodoCatalogoRequest): Observable<PeriodoCatalogo> {
    return this.http.post<PeriodoCatalogo>(`${this.API_ADMIN_CATALOGOS}/periodos`, payload);
  }

  actualizarPeriodoCatalogo(id: number, payload: PeriodoCatalogoRequest): Observable<PeriodoCatalogo> {
    return this.http.put<PeriodoCatalogo>(`${this.API_ADMIN_CATALOGOS}/periodos/${id}`, payload);
  }

  desactivarPeriodoCatalogo(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_ADMIN_CATALOGOS}/periodos/${id}/desactivar`, {});
  }

}
