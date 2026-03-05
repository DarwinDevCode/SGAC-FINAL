export interface DocenteDTO {
  idDocente: number;
  idUsuario: number;
  nombreCompletoUsuario: string;
}

export interface DocenteDashboardDTO {
  totalAyudantes: number;
  actividadesPendientes: number;
  actividadesAceptadas: number;
  actividadesRechazadas: number;
  actividadesObservadas: number;
  totalActividades: number;
}

export interface AyudanteResumenDTO {
  idAyudantia: number;
  idUsuario: number;
  nombreCompleto: string;
  correo: string;
  nombreAsignatura: string;
  estadoAyudantia: string;
  horasCumplidas: number;
  actividadesTotal: number;
  actividadesPendientes: number;
}

export interface EvidenciaDocenteDTO {
  idEvidencia: number;
  tipoEvidencia: string;
  nombreArchivo: string;
  rutaArchivo: string;
  mimeType: string;
  fechaSubida: string;
  estadoEvidencia: string;
  observaciones?: string;
  fechaObservacion?: string;
}

export interface RegistroActividadDocenteDTO {
  idRegistroActividad: number;
  idAyudantia: number;
  descripcionActividad: string;
  temaTratado: string;
  fecha: string;
  numeroAsistentes: number;
  horasDedicadas: number;
  estadoRevision: string;
  observaciones?: string;
  fechaObservacion?: string;
  evidencias: EvidenciaDocenteDTO[];
  expandido?: boolean; // UI state
}

export interface CambiarEstadoRequest {
  estado: string;
  observaciones?: string;
}

export interface ObservacionWsDTO {
  tipo: 'ACTIVIDAD' | 'EVIDENCIA';
  idReferencia: number;
  nombreActividad: string;
  observacion: string;
  estadoNuevo: string;
  fecha: string;
  nombreDocente: string;
}
