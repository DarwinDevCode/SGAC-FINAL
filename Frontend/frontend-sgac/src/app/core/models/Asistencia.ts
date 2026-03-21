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
  yaExiste:       boolean;
}

export interface PreviewResponse {
  exito:             boolean;
  tieneErrores:      boolean;
  totalFilas:        number;
  nuevos:            number;
  duplicadosBD:      number;
  duplicadosArchivo: number;
  filas:             FilaPreview[];
  mensaje:           string;
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

export interface SesionMatriz {
  id:    number;
  fecha: string;
  tema:  string;
  horas: number;
}

export interface EstudianteMatriz {
  idParticipante: number;
  nombre:         string;
  curso:          string;
  paralelo:       string;
  asistencias:    Record<string, boolean | null>;
}

export interface MatrizAsistencia {
  sesiones:    SesionMatriz[];
  estudiantes: EstudianteMatriz[];
}
