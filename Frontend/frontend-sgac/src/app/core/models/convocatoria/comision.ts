export interface MiembroComision {
  idUsuario: number;
  nombres:   string;
  apellidos: string;
  cargo:     string;
  correo:    string;
}

export interface FaseEvaluacion {
  nombreFase:  string;
  codigoFase:  string;
  fechaInicio: string;
  fechaFin:    string;
}

export interface PostulanteComision {
  idPostulacion:    number;
  nombres:          string;
  apellidos:        string;
  correo:           string;
  fechaPostulacion: string;
  estadoPostulacion: string;
  codigoEstado:      string;
}

export interface ComisionEstudiante {
  idComision:        number;
  idConvocatoria:    number;
  nombreAsignatura:  string;
  nombreComision:    string;
  fechaConformacion: string;
  miembros:          MiembroComision[];
}

export interface ConvocatoriaComision {
  idConvocatoria:    number;
  nombreAsignatura:  string;
  idComision:        number;
  nombreComision:    string;
  fechaConformacion: string;
  faseEvaluacion:    FaseEvaluacion | null;
  postulantes:       PostulanteComision[];
}

export interface ComisionDetalleResponse {
  exito:         boolean;
  mensaje?:      string;
  rol:           string;
  comisiones?:   ComisionEstudiante[];
  convocatorias?: ConvocatoriaComision[];
}

export interface GenerarComisionesResponse {
  exito:                  boolean;
  mensaje:                string;
  comisionesCreadas:      number;
  convocatoriasOmitidas?: number;
}
