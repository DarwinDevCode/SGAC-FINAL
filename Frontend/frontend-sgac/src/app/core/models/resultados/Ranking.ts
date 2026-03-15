export type EstadoRanking = 'SELECCIONADO' | 'ELEGIBLE' | 'NO_SELECCIONADO';
export interface ResultadoRanking {
  posicion:         number;
  postulante:       string;
  meritos:          number;
  oposicion:        number;
  total:            number;
  estado:           EstadoRanking;
  asignatura:       string;
  semestre:         number;
  carrera:          string;
  facultad:         string;
  cuposDisponibles: number;
}

export interface RankingResponse {
  exito:            boolean;
  mensaje?:         string;
  faseNoPublicada?: boolean;
  resultados?:      ResultadoRanking[];
}
