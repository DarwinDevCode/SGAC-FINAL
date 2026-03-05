import {Evidencia} from './evidencia';

export interface SesionDetalle {
  idRegistro:        number;
  idAyudantia:       number;
  fecha:             string;
  temaTratado:       string;
  descripcionActividad:       string;
  horasDedicadas:    number;
  numeroAsistentes:  number;
  estado:    string;
  observacion?: string
  fechaRegistro: string;
  //nombreAsignatura:  string;
  //nombreDocente:     string;
  //nombrePeriodo:     string;

  evidencias:        Evidencia[];
}
