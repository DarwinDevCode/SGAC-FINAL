import {TipoEstadoRegistro} from './tipo-estado-registro';
import {Evidencia} from './evidencia';

export interface SesionDetalle {
  idRegistro:        number;
  fecha:             string;
  temaTratado:       string;
  descripcion:       string;
  numeroAsistentes:  number;
  horasDedicadas:    number;
  estado:    string;

  nombreAsignatura:  string;
  nombreDocente:     string;
  nombrePeriodo:     string;

  evidencias:        Evidencia[];
}
