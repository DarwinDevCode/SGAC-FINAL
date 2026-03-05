import {TipoEstadoEvidencia} from './tipo-estado-evidencia';

export interface Evidencia {
  idEvidencia:      number;
  idTipoEvidencia:  number;
  nombreTipoEvidencia:    string;
  nombreArchivo:    string;
  rutaArchivo:      string;
  mimeType:         string;
  tamanioBytes:     number;
  estado:    string;
}
