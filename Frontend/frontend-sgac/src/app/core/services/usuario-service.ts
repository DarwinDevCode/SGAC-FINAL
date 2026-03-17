import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RegistroUsuarioGlobalDTO, UsuarioDTO } from '../dto/usuario';
import { TipoRolDTO } from '../dto/tipo-rol';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly API_AUTH = `${this.baseUrl}/auth`;

  getRolesActivos(): Observable<TipoRolDTO[]> {
    return this.http.get<TipoRolDTO[]>(`${this.API_AUTH}/roles-activos`);
  }

  registrarGlobal(dto: RegistroUsuarioGlobalDTO): Observable<{ mensaje: string; exito: boolean }> {
    return this.http.post<{ mensaje: string; exito: boolean }>(
      `${this.API_AUTH}/registro-global`,
      dto
    );
  }

  listarUsuarios(): Observable<UsuarioDTO[]> {
    return this.http.get<UsuarioDTO[]>(this.API_AUTH);
  }

  cambiarEstado(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_AUTH}/${id}/estado`, {});
  }

  cambiarEstadoRol(idUsuario: number, idTipoRol: number): Observable<void> {
    return this.http.patch<void>(`${this.API_AUTH}/${idUsuario}/roles/${idTipoRol}/estado`, {});
  }

  crear(usuario: UsuarioDTO): Observable<UsuarioDTO> {
    const rol = usuario.rolRegistro;
    let endpoint = '';
    const payload: Record<string, unknown> = {
      nombres: usuario.nombres, apellidos: usuario.apellidos,
      cedula: usuario.cedula, correo: usuario.correo,
      username: usuario.nombreUsuario, password: usuario.password,
    };
    switch (rol) {
      case 'ESTUDIANTE':
        endpoint = '/registro-estudiante';
        payload['idCarrera'] = usuario.idCarrera;
        payload['matricula'] = usuario.matricula;
        payload['semestre']  = usuario.semestre;
        break;
      case 'DOCENTE':      endpoint = '/registro-docente';    break;
      case 'COORDINADOR':  endpoint = '/registro-coordinador'; payload['idCarrera'] = usuario.idCarrera; break;
      case 'DECANO':       endpoint = '/registro-decano';     payload['idFacultad'] = usuario.idFacultad; break;
      case 'ADMINISTRADOR':endpoint = '/registro-admin';      break;
      case 'AYUDANTE_CATEDRA': endpoint = '/registro-ayudante-directo'; payload['horasAyudante'] = usuario.horasAyudante; break;
      default: throw new Error(`Rol no soportado: ${rol}`);
    }
    return this.http.post<UsuarioDTO>(`${this.API_AUTH}${endpoint}`, payload);
  }
}
