import {CarreraDTO} from './carrera';

export interface AsignaturaDTO {
  idAsignatura?: number;
  nombreAsignatura: string;
  semestre: number;
  activo?: boolean;
  idCarrera?: number;
  carrera?: CarreraDTO;
  nombreCarrera?: string;
}
