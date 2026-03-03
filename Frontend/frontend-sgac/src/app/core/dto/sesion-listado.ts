import {TipoEstadoRegistro} from './tipo-estado-registro';

export interface SesionListado {
  idRegistro:        number;
  fecha:             string;
  temaTratado:       string;
  horasDedicadas:    number;
  numeroAsistentes:  number;
  estado:    string;
  tieneObservacion:  boolean;
  totalEvidencias:   number;
}
