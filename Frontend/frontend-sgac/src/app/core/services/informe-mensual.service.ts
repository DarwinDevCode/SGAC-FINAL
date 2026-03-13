import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InformeMensualResponse } from '../dto/informe-mensual-response';

@Injectable({
    providedIn: 'root'
})
export class InformeMensualService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/api/informes`;

    listarPorAyudantia(idAyudantia: number): Observable<InformeMensualResponse[]> {
        return this.http.get<InformeMensualResponse[]>(`${this.apiUrl}/ayudantia/${idAyudantia}`);
    }

    listarMisInformes(idUsuario: number): Observable<InformeMensualResponse[]> {
        return this.http.get<InformeMensualResponse[]>(`${this.apiUrl}/mis-informes?idUsuario=${idUsuario}`);
    }

    obtenerDetalle(id: number): Observable<InformeMensualResponse> {
        return this.http.get<InformeMensualResponse>(`${this.apiUrl}/${id}`);
    }

    generarBorradorIA(idAyudantia: number, mes: number, anio: number): Observable<InformeMensualResponse> {
        return this.http.post<InformeMensualResponse>(`${this.apiUrl}/ayudantia/${idAyudantia}/generar?mes=${mes}&anio=${anio}`, {});
    }

    enviarARevision(id: number, borradorEditado: string): Observable<InformeMensualResponse> {
        return this.http.post<InformeMensualResponse>(`${this.apiUrl}/${id}/enviar`, { borradorEditado });
    }

    aprobarInforme(id: number, rol: string): Observable<InformeMensualResponse> {
        return this.http.post<InformeMensualResponse>(`${this.apiUrl}/${id}/aprobar?rol=${rol}`, {});
    }

    observarInforme(id: number, observaciones: string): Observable<InformeMensualResponse> {
        return this.http.post<InformeMensualResponse>(`${this.apiUrl}/${id}/observar`, { observaciones });
    }

    rechazarInforme(id: number, observaciones: string): Observable<InformeMensualResponse> {
        return this.http.post<InformeMensualResponse>(`${this.apiUrl}/${id}/rechazar`, { observaciones });
    }

    listarPendientesDocente(idDocente: number): Observable<InformeMensualResponse[]> {
        return this.http.get<InformeMensualResponse[]>(`${this.apiUrl}/docente/${idDocente}/pendientes`);
    }

    listarPendientesCoordinador(): Observable<InformeMensualResponse[]> {
        return this.http.get<InformeMensualResponse[]>(`${this.apiUrl}/coordinador/pendientes`);
    }

    listarPendientesDecano(): Observable<InformeMensualResponse[]> {
        return this.http.get<InformeMensualResponse[]>(`${this.apiUrl}/decano/pendientes`);
    }
}
