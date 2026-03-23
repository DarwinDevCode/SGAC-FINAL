import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {RespuestaOperacion} from '../../models/general/respuesta-operacion';


@Injectable({
  providedIn: 'root'
})
export class BackupService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/configuracion/respaldos`;


  listarRespaldos(): Observable<string[]> {
    return this.http.get<string[]>(this.apiUrl);
  }

  generarRespaldo(): Observable<RespuestaOperacion<string>> {
    return this.http.post<RespuestaOperacion<string>>(`${this.apiUrl}/generar`, {});
  }

  restaurarRespaldo(nombreArchivo: string): Observable<RespuestaOperacion<void>> {
    return this.http.post<RespuestaOperacion<void>>(`${this.apiUrl}/restaurar/${nombreArchivo}`, {});
  }
}
