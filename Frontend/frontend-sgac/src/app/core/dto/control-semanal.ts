export interface ControlSemanal {
  semanaInicio:           string;
  semanaFin:              string;
  horasRegistradas:       number;
  horasAprobadasSemana:   number;
  horasPendientesSemana:  number;
  limiteSemanal:          number;
  horasDisponibles:       number;
  superaLimite:           boolean;
  sesionesSemana:         number;
}
