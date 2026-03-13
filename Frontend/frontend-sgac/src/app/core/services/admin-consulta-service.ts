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

export interface AdminConsultaDTO {
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
export class AdminConsultaService {
    private http = inject(HttpClient);
    private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';

    obtenerEstadisticas(): Observable<AdminConsultaDTO> {
        return this.http.get<AdminConsultaDTO>(`${this.baseUrl}/admin/consulta/estadisticas`);
    }

    descargarReporteDashboard(): Observable<Blob> {
        return this.http.get(`${this.baseUrl}/metricas/reporte-admin`, { responseType: 'blob' });
    }

    descargarReporteExcel(): Observable<Blob> {
        return this.http.get(`${this.baseUrl}/metricas/reporte-admin/excel`, { responseType: 'blob' as 'json' }) as unknown as Observable<Blob>;
    }
}
