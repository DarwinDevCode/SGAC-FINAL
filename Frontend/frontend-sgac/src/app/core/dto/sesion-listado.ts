import {TipoEstadoRegistro} from './tipo-estado-registro';

export interface SesionListado {
  idRegistro:        number;
  fecha:             string;
  temaTratado:       string;
  descripcion:       string;
  numeroAsistentes:  number;
  horasDedicadas:    number;
  estado:    string;
  totalEvidencias:   number;
  tieneObservacion:  boolean;
}
