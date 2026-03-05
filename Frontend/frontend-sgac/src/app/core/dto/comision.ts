export interface ComisionDTO {
    idComisionSeleccion: number;
    idConvocatoria: number;
    nombreComision: string;
    fechaConformacion: string; // ISO date YYYY-MM-DD
    activo: boolean;
}

export interface ComisionRequestDTO {
    idConvocatoria: number;
    nombreComision: string;
    fechaConformacion: string;
    activo: boolean;
}
