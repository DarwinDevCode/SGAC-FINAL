import {EvidenciaRequest} from './evidencia-request';

export interface RegistrarSesionResponse {
  exito:             boolean;
  mensaje:           string;
  idRegistroCreado:  number | null;
}
