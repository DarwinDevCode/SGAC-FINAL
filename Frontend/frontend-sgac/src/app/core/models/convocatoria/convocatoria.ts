export interface ConvocatoriaDTO {
  idConvocatoria?:       number;
  nombrePeriodo?:        string;
  nombreAsignatura?:     string;
  idPeriodoAcademico?:   number;
  idAsignatura?:         number;
  idDocente?:            number;
  nombreDocente?:        string;
  idUsuarioDocente?:     number;
  idUsuarioCoordinador?: number;
  cuposDisponibles?:     number;
  estado?:               string;
  activo?:               boolean;
}

export interface ConvocatoriaCrearRequest {
  idAsignatura:     number;
  idDocente:        number;
  cuposDisponibles: number;
  estado?:          string;
}

export interface ConvocatoriaActualizarRequest {
  idConvocatoria:   number;
  tipoEdicion:      'PARCIAL' | 'COMPLETA';
  cuposDisponibles?: number;
  estado?:           string;
  idDocente?:        number;
  idAsignatura?:     number;
}

export interface ConvocatoriaNativaResponse {
  exito:   boolean;
  mensaje: string;
  id?:     number;
}

export interface VerificarFaseResponse {
  valido:          boolean;
  mensaje:         string;
  idPeriodo?:      number;
  nombrePeriodo?:  string;
  codigoFase?:     string;
  nombreFase?:     string;
  faseInicio?:     string;
  faseFin?:        string;
}

export interface VerificarPostulantesResponse {
  exito:            boolean;
  tienePostulantes: boolean;
  totalPostulantes: number;
  mensaje:          string;
}
