import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
    AyudanteCatedraResponseDTO,
    AyudantiaResponseDTO,
    RegistroActividadRequestDTO,
    RegistroActividadResponseDTO
} from '../dto/ayudante';

@Injectable({
    providedIn: 'root'
})
export class AyudanteService {
    private http = inject(HttpClient);

    private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
    private readonly API_AYUDANTES = `${this.baseUrl}/ayudantes`;

    obtenerAyudantePorUsuario(idUsuario: number): Observable<AyudanteCatedraResponseDTO> {
        return this.http.get<AyudanteCatedraResponseDTO>(`${this.API_AYUDANTES}/usuario/${idUsuario}`);
    }

    obtenerAyudantiaPorPostulacion(idPostulacion: number): Observable<AyudantiaResponseDTO> {
        return this.http.get<AyudantiaResponseDTO>(`${this.API_AYUDANTES}/ayudantia/postulacion/${idPostulacion}`);
    }

    listarActividades(idAyudantia: number): Observable<RegistroActividadResponseDTO[]> {
        return this.http.get<RegistroActividadResponseDTO[]>(`${this.API_AYUDANTES}/ayudantia/${idAyudantia}/actividades`);
    }

    registrarActividad(request: RegistroActividadRequestDTO): Observable<RegistroActividadResponseDTO> {
        return this.http.post<RegistroActividadResponseDTO>(`${this.API_AYUDANTES}/actividades`, request);
    }

    actualizarActividad(id: number, request: RegistroActividadRequestDTO): Observable<RegistroActividadResponseDTO> {
        return this.http.put<RegistroActividadResponseDTO>(`${this.API_AYUDANTES}/actividades/${id}`, request);
    }

    eliminarActividad(id: number): Observable<void> {
        return this.http.delete<void>(`${this.API_AYUDANTES}/actividades/${id}`);
    }
}
