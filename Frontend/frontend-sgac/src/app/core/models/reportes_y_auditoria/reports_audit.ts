
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface AuditoriaResponseDTO {
  idLogAuditoria: number;
  fechaHora: string;
  usuarioEjecutor: string;
  cedula: string;
  accion: 'INSERT' | 'UPDATE' | 'DELETE';
  tablaAfectada: string;
  ipOrigen: string;
  valorAnterior: Record<string, any> | null;
  valorNuevo: Record<string, any> | null;
}

export interface EvolucionAuditoria {
  fecha: string;
  inserts: number;
  updates: number;
  deletes: number;
}

export interface AuditoriaKpiDTO {
  totalRegistros: number;
  actividadHoy: number;
  totalInserts: number;
  totalUpdates: number;
  totalDeletes: number;
}
