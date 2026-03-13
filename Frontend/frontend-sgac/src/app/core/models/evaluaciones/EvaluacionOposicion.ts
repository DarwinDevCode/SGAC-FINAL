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
}

export interface SorteoPayload {
  idConvocatoria: number;
  fecha:          string;
  horaInicio:     string;
  lugar:          string;
}
