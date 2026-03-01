export interface EvidenciaRequest {
  idTipoEvidencia:  number;
  nombreArchivo:    string;
  rutaArchivo:      string;
  mimeType?:        string;
  tamanioBytes?:    number;
}
