import {EvidenciaRequest} from './evidencia-request';

export interface RegistrarSesionRequest {
  fecha:                string;
  temaTratado:          string;
  descripcionActividad: string;
  numeroAsistentes:     number;
  horasDedicadas:       number;
  evidencias:           EvidenciaRequest[];
}
