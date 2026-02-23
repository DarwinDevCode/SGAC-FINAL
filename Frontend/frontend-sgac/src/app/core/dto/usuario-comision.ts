export interface UsuarioComisionDTO {
  idUsuarioComision: number;
  idComisionSeleccion: number;
  nombreComision: string;
  idUsuario: number;
  nombreCompletoUsuario: string;
  idEvaluacionOposicion?: number;
  rolIntegrante: string;
  puntajeMaterial?: number;
  puntajeRespuestas?: number;
  puntajeExposicion?: number;
  fechaEvaluacion?: string;
  idConvocatoria?: number;
  asignatura?: string;
}
