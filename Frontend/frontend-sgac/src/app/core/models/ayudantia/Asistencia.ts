export interface ContextoAsistencia {
  idAyudantia: number;
  idRegistro:  number;
}

export interface Participante {
  idParticipante: number;
  idAyudantia:    number;
  nombreCompleto: string;
  curso:          string;
  paralelo:       string;
  activo:         boolean;
}

export interface FilaPreview {
  fila:           number;
  nombreCompleto: string;
  curso:          string;
  paralelo:       string;
  errores:        string[];
  valida:         boolean;
}

export interface PreviewResponse {
  exito:        boolean;
  tieneErrores: boolean;
  totalFilas:   number;
  filas:        FilaPreview[];
  mensaje:      string;
}

export interface CargaMasivaResponse {
  exito:      boolean;
  mensaje:    string;
  insertados: number;
  duplicados: number;
  total:      number;
}

export interface DetalleAsistencia {
  idDetalle:      number;
  idParticipante: number;
  nombreCompleto: string;
  curso:          string;
  paralelo:       string;
  asistio:        boolean;
}

export interface GuardarAsistenciaResponse {
  exito:     boolean;
  mensaje:   string;
  presentes: number;
  total:     number;
}
