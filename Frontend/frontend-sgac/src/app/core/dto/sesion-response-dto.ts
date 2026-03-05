import { EvidenciaResponseDTO } from './evidencia-response-dto';

export interface SesionResponseDTO {
  idRegistroActividad: number;
  descripcionActividad?: string;
  temaTratado?: string;
  fecha?: string;
  numeroAsistentes?: number;
  horasDedicadas?: number;
  idTipoEstadoRegistro?: number;
  nombreEstado?: string;
  estadoRevision?: string;
  observacionDocente?: string;
  fechaObservacion?: string;
  evidencias: EvidenciaResponseDTO[];
}
