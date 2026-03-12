import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EstadisticaMensualDTO {
    mes: string;
    postulaciones: number;
    convocatorias: number;
}

export interface RolEstadisticaDTO {
    rol: string;
    cantidad: number;
}

export interface FacultadEstadisticaDTO {
    facultad: string;
    cantidadPostulaciones: number;
}

export interface LogActividadDTO {
    idLog: number;
    usuario: string;
    accion: string;
    modulo: string;
    fecha: string;
}

export interface AdminDashboardDTO {
    totalUsuarios: number;
    totalPostulaciones: number;
    totalConvocatorias: number;
    periodoActivo: string;
    estadisticasMensuales: EstadisticaMensualDTO[];
    distribucionRoles: RolEstadisticaDTO[];
    distribucionFacultades: FacultadEstadisticaDTO[];
    ultimasAcciones: LogActividadDTO[];
}

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
    private http = inject(HttpClient);
    private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

    obtenerEstadisticas(): Observable<AdminDashboardDTO> {
        return this.http.get<AdminDashboardDTO>(`${this.baseUrl}/admin/dashboard/estadisticas`);
    }
}
