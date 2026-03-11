export interface CronogramaActivoResponse {
  exito:    boolean;
  mensaje?: string;
  periodo?: PeriodoInfo;
  fases?:   FaseInfo[];
}

export interface PeriodoInfo {
  id:                number;
  nombre:            string;
  fechaInicio:       string;
  fechaFin:          string;
  diasTranscurridos: number;
  diasTotales:       number;
  porcentajeAvance:  number;
}

export interface FaseInfo {
  idPeriodoFase: number;
  idTipoFase:    number;
  orden:         number;
  codigo:        string;
  nombre:        string;
  descripcion:   string;
  fechaInicio:   string;
  fechaFin:      string;
  duracionDias:  number;
  esActual:      boolean;
}
