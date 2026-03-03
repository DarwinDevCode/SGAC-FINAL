import { EvidenciaRequest } from './evidencia-request';

export interface RegistrarSesionRequest {
  /**
   * El backend lo valida como obligatorio.
   * Si el backend lo calcula a partir de idUsuario, puede enviarse 0 como placeholder.
   */
  idAyudantia: number;

  descripcionActividad: string;
  temaTratado: string;

  /** Fecha en formato ISO (yyyy-MM-dd) */
  fecha: string;

  numeroAsistentes: number;
  horasDedicadas: number;

  /** Debe tener la misma longitud e índice que el arreglo de archivos adjuntos */
  evidencias: EvidenciaRequest[];
}
