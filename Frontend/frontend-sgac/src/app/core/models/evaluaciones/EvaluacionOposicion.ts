// src/app/core/models/evaluaciones/EvaluacionOposicion.ts

export interface TurnoOposicion {
  idEvaluacionOposicion: number;
  orden:                 number;
  nombres:               string;
  apellidos:             string;
  correo:                string;
  tema:                  string;
  fecha:                 string;
  horaInicio:            string;
  horaFin:               string;
  horaInicioReal?:       string;
  horaFinReal?:          string;
  lugar:                 string;
  estado:                string;
  nombreEstado:          string;
  puntajeFinal?:         number;
  jurados:               PuntajeJurado[];

  /**
   * ISO-8601 UTC del instante en que el servidor registró hora_inicio_real.
   * Presente solo cuando estado === 'EN_CURSO'.
   * Se usa en la lógica del timer para resistir el efecto F5:
   *
   *   segsTranscurridos = Math.floor((Date.now() - new Date(serverTimestamp)) / 1000)
   *   timerSegundos     = Math.min(segsTranscurridos, BLOQUE_TOTAL_SEG)
   */
  serverTimestamp?:      string;
}

export interface PuntajeJurado {
  idUsuario:           number;
  nombres:             string;
  apellidos:           string;
  rol:                 string;
  puntajeMaterial?:    number;
  puntajeExposicion?:  number;
  puntajeRespuestas?:  number;
  subtotal:            number;
  finalizo:            boolean;
}

export interface TemaOposicion {
  idTema:          number;
  descripcionTema: string;
  activo:          boolean;
}

export interface OposicionResponse {
  exito:                boolean;
  mensaje?:             string;
  temas?:               TemaOposicion[];
  totalTemas?:          number;
  totalAptos?:          number;
  listoParaSorteo?:     boolean;
  turnos?:              number;
  fecha?:               string;
  horaInicio?:          string;
  lugar?:               string;
  cronograma?:          TurnoOposicion[];
  todosFinalizaron?:    boolean;
  puntajeFinal?:        number;
  subtotal?:            number;
  horaReal?:            string;
  horaFin?:             string;
}

export interface PuntajeJuradoPayload {
  idEvaluacionOposicion: number;
  idUsuario:             number;
  puntajeMaterial:       number;
  puntajeExposicion:     number;
  puntajeRespuestas:     number;
  finalizar:             boolean;
  /** Opcional: para activar el broadcast WS desde el backend */
  idConvocatoria?:       number;
}

export interface SorteoPayload {
  idConvocatoria: number;
  fecha:          string;
  horaInicio:     string;
  lugar:          string;
}

export interface ConvocatoriaOposicionDTO {
  idConvocatoria:        number;
  nombreAsignatura:      string;
  semestreAsignatura:    number;
  nombreCarrera:         string;
  nombreFacultad:        string;
  nombreDocente:         string;
  cuposDisponibles:      number;
  estadoConvocatoria:    string;
  totalPostulantesAptos: number;
  tieneComision:         boolean;
  tieneSorteo:           boolean;
}

export interface ConvocatoriasAptasResponse {
  exito:    boolean;
  mensaje?: string;
  datos?:   ConvocatoriaOposicionDTO[];
}
