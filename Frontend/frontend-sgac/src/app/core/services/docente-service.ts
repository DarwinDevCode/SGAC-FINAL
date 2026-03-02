import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DocenteDTO } from '../dto/docente';

@Injectable({
  providedIn: 'root',
})
export class DocenteService {
  private http = inject(HttpClient);
  private readonly API = `${(environment as any).apiUrl || 'http://localhost:8080/api'}/docentes`;

  listarActivos(): Observable<DocenteDTO[]> {
    return this.http.get<DocenteDTO[]>(this.API);
  }
}
