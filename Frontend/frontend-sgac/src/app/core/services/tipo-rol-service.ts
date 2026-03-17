import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RolResumenDTO } from '../dto/rol-resumen-dto';

@Injectable({
  providedIn: 'root',
})
export class TipoRolService {
  private readonly baseUrl = environment.apiUrl;
  private apiUrl = `${this.baseUrl}/tipos-rol`;
  http = inject(HttpClient)

  obtenerRolesParaPermisos(): Observable<RolResumenDTO[]> {
    return this.http.get<RolResumenDTO[]>(`${this.apiUrl}/resumen-permisos`);
  }
}
