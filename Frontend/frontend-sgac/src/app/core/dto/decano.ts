export interface DecanoResponseDTO {
    idDecano: number;
    idUsuario: number;
    nombreCompleto: string; // As mapped by backend
    idFacultad: number;
    nombreFacultad: string; // As mapped by backend 
    fechaInicio: string;
    fechaFin: string;
    activo: boolean;
}
