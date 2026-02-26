export interface AyudanteCatedraResponseDTO {
    idAyudanteCatedra: number;
    idUsuario: number;
    nombreCompletoUsuario: string;
    cedulaUsuario: string;
    horasAyudante: number;
}

export interface AyudantiaResponseDTO {
    idAyudantia: number;
    idTipoEstadoEvidenciaAyudantia: number;
    nombreEstadoEvidencia: string;
    idPostulacion: number;
    nombreEstudiante: string;
    fechaInicio: string;
    fechaFin: string;
    horasCumplidas: number;
}

export interface RegistroActividadRequestDTO {
    idAyudantia: number;
    descripcionActividad: string;
    temaTratado: string;
    fecha: string;
    numeroAsistentes: number;
    horasDedicadas: number;
}

export interface RegistroActividadResponseDTO extends RegistroActividadRequestDTO {
    idRegistroActividad: number;
    estadoRevision: string;
}
