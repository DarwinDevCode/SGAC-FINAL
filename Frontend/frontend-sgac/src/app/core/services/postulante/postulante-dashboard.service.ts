import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PostulanteDashboard } from '../../models/postulante/postulante-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class PostulanteDashboardService {
  private http = inject(HttpClient);

  private readonly baseUrl = 'http://localhost:8080/api/estudiante/dashboard';

  getResumenDashboard(): Observable<PostulanteDashboard> {
    return this.http.get<PostulanteDashboard>(`${this.baseUrl}/resumen`);
  }
}
