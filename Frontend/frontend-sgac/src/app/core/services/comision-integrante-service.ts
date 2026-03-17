import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UsuarioComisionDTO } from '../dto/usuario-comision';
import { UsuarioComisionRequestDTO } from '../dto/usuario-comision-request';

@Injectable({
    providedIn: 'root',
})
export class ComisionIntegranteService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly API = `${this.baseUrl}/comisiones/integrantes`;

    asignar(dto: UsuarioComisionRequestDTO): Observable<UsuarioComisionDTO> {
        return this.http.post<UsuarioComisionDTO>(this.API, dto);
    }

    listarPorComision(idComision: number): Observable<UsuarioComisionDTO[]> {
        return this.http.get<UsuarioComisionDTO[]>(`${this.API}/comision/${idComision}`);
    }

    remover(idUsuarioComision: number): Observable<any> {
        return this.http.delete(`${this.API}/${idUsuarioComision}`, { responseType: 'text' });
    }
}
