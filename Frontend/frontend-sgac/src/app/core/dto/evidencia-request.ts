export interface EvidenciaRequest {
  idTipoEvidencia: number;
  nombreArchivo: string;

  /** Se completa en backend (Cloudinary) */
  rutaArchivo?: string;

  /** Se completa en backend (Cloudinary) */
  mimeType?: string;

  /** Se completa en backend (Cloudinary) */
  tamanioBytes?: number;
}
