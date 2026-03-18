import { EvidenciaRequest } from './evidencia-request';

export interface RegistrarSesionRequest {
  descripcionActividad: string;
  temaTratado: string;
  fecha: string;
  numeroAsistentes: number;
  horasDedicadas: number;
  evidencias: EvidenciaRequest[];
}
