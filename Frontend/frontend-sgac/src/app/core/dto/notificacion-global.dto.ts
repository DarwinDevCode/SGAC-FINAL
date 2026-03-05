export interface NotificacionGlobalDTO {
  idNotificacion: number;
  titulo: string;
  mensaje: string;
  tipo: string;
  idReferencia: number | null;
  leido: boolean;
  fechaCreacion: string;
  fechaLectura?: string | null;
}
