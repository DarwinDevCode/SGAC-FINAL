import {TipoRolDTO} from './tipo-rol';

export interface UsuarioDTO {
  idUsuario?: number;
  rolActual?: string;
  roles?: TipoRolDTO[];
  token?: string;
  activo?: boolean;

  nombres: string;
  apellidos: string;
  correo: string;
  cedula: string;
  nombreUsuario: string;

  password?: string;
  rolRegistro?: string;

  idCarrera?: number;
  idFacultad?: number;
  semestre?: number;
  matricula?: string;
  horasAyudante?: number;
}
