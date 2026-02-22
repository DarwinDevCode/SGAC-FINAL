import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { UsuarioDTO} from '../dto/usuario';
import {FacultadDTO} from '../dto/facultad';
import {CarreraDTO} from '../dto/carrera';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private http = inject(HttpClient);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_AUTH = `${this.baseUrl}/auth`;
  private readonly API_RECURSOS = `${this.baseUrl}/recursos`;

  listarUsuarios(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(this.API_AUTH);
  }

  crear(usuario: UsuarioDTO): Observable<UsuarioDTO> {
    const rol = usuario.rolRegistro;
    let endpoint = '';

    const payload: any = {
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
        payload.idCarrera = usuario.idCarrera;
        payload.matricula = usuario.matricula;
        payload.semestre = usuario.semestre;
        break;
      case 'DOCENTE':
        endpoint = '/registro-docente';
        break;
      case 'COORDINADOR':
        endpoint = '/registro-coordinador';
        payload.idCarrera = usuario.idCarrera;
        break;
      case 'DECANO':
        endpoint = '/registro-decano';
        payload.idFacultad = usuario.idFacultad;
        break;
      case 'ADMINISTRADOR':
        endpoint = '/registro-admin';
        break;
      case 'AYUDANTE_CATEDRA':
        endpoint = '/registro-ayudante-directo';
        payload.horasAyudante = usuario.horasAyudante;
        break;
      default:
        throw new Error(`Rol no soportado: ${rol}`);
    }

    return this.http.post<UsuarioDTO>(`${this.API_AUTH}${endpoint}`, payload);
  }

  cambiarEstado(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_AUTH}/${id}/estado`, {});
  }

  cambiarEstadoRol(idUsuario: number, idTipoRol: number): Observable<void> {
    return this.http.patch<void>(`${this.API_AUTH}/${idUsuario}/roles/${idTipoRol}/estado`, {});
  }

  listarFacultades(): Observable<FacultadDTO[]> {
    return this.http.get<FacultadDTO[]>(`${this.API_RECURSOS}/facultades`);
  }

  listarCarreras(): Observable<CarreraDTO[]> {
    return this.http.get<CarreraDTO[]>(`${this.API_RECURSOS}/carreras`);
  }
}
