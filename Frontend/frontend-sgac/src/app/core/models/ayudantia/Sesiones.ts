export interface EvidenciaResponseDTO {
  idEvidenciaRegistroActividad: number;
  nombreArchivo:         string;
  rutaArchivo:           string;
  mimeType:              string;
  fechaSubida:           string;
  codigoEstadoEvidencia: string;
  nombreEstadoEvidencia: string;
  observacionDocente:    string | null;
  fechaObservacion:      string | null;
}

export interface SesionResponseDTO {
  idRegistroActividad:  number;
  fecha:                string;
  horaInicio:           string | null;
  horaFin:              string | null;
  horasDedicadas:       number | null;
  temaTratado:          string;
  lugar:                string | null;
  descripcionActividad: string | null;
  observacionDocente:   string | null;
  fechaObservacion:     string | null;
  codigoEstado:         string;
  nombreEstado:         string;
  evidencias:           EvidenciaResponseDTO[] | null;
}

export interface PlanificarSesionRequest {
  idAyudantia: number;
  fecha:       string;
  horaInicio:  string;
  horaFin:     string;
  lugar:       string;
  temaTratado: string;
}

export interface AsistenciaItem {
  idParticipanteAyudantia: number;
  asistio:                 boolean;
}

export interface EvidenciaMetadata {
  idTipoEvidencia:         number;
  nombreArchivoReferencia: string;
}

export interface CompletarSesionRequest {
  descripcionActividad:  string;
  asistencias:           AsistenciaItem[];
  metadatosEvidencias:   EvidenciaMetadata[];
}

export interface EvaluarSesionRequest {
  codigoEstado:  string;
  observaciones: string | null;
}

export interface PlanificarSesionResponse {
  exito:            boolean;
  mensaje:          string;
  idRegistroCreado: number;
}

export interface CompletarSesionResponse {
  exito:   boolean;
  mensaje: string;
}

export interface EvaluarSesionResponse {
  exito:   boolean;
  mensaje: string;
}
