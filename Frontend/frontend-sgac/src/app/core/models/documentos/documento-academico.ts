export interface TipoDocumentoResponse {
  id:     number;
  nombre: string;
  codigo: string;
}

export interface ConvocatoriaActivaResponse {
  idConvocatoria:   number;
  nombreAsignatura: string;
  nombreDocente:    string;
  estado:           string;
  cuposDisponibles: number;
}

export interface DocumentoResponse {
  id:              number;
  nombreMostrar:   string;
  rutaArchivo:     string;
  extension:       string | null;
  pesoBytes:       number | null;
  fechaSubida:     string;
  idTipoDocumento: number;
  tipoNombre:      string;
  tipoCodigo:      string;
  idPeriodo:       number;
  idConvocatoria:  number | null;
  esGlobal:        boolean;
}
