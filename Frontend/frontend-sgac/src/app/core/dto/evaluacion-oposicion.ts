import {UsuarioComisionDTO} from './usuario-comision';

export interface EvaluacionOposicionDTO {
  idEvaluacionOposicion?: number;
  idPostulacion: number;
  temaExposicion: string;
  fechaEvaluacion: string;
  horaInicio: string;
  horaFin: string;
  lugar: string;
  estado: string;
  evaluacionesComision?: UsuarioComisionDTO[];
}
