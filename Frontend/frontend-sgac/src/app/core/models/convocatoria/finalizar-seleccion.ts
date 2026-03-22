export interface ResumenSeleccion {
  idConvocatoria:    number;
  seleccionados:     number;
  elegibles:         number;
  noSeleccionados:   number;
  ayudantiasAbiertas: number;
  mensaje:           string;
}

export interface FinalizarSeleccionResponse {
  exito:   boolean;
  mensaje: string;
  resumen: ResumenSeleccion | null;
}
