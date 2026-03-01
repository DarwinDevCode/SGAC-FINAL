import {TipoEstadoEvidencia} from './tipo-estado-evidencia';

export interface Evidencia {
  idEvidencia:      number;
  nombreArchivo:    string;
  rutaArchivo:      string;
  mimeType:         string;
  tamanioBytes:     number;
  tipoEvidencia:    string;
  estado:    string;
  fechaSubida:      string;
}
