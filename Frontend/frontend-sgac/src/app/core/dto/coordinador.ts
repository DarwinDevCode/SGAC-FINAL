export interface CoordinadorResponseDTO {
    idCoordinador: number;
    idUsuario: number;
    nombreCompletoUsuario: string;
    cedula: string;
    idCarrera: number;
    nombreCarrera: string;
    fechaInicio: string; // LocalDate as ISO string
    fechaFin: string; // LocalDate as ISO string
    activo: boolean;
}
