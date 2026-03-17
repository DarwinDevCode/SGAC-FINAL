import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { InformeMensualService, InformeMensual } from '../../../core/services/informe-mensual-service';
import { AuthService } from '../../../core/services/auth-service';

@Component({
  selector: 'app-ayudante-informes',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="gestion-container">
      <div class="section-header">
        <div class="title-container">
          <h2 class="title">Mis Informes</h2>
          <p class="subtitle">Informes mensuales de actividades de ayudantía.</p>
        </div>
      </div>

      <div style="display:flex;flex-direction:column;gap:.75rem">
        @for (informe of informes; track informe.idInformeMensual) {
          <div class="informe-card">
            <div style="display:flex;align-items:center;gap:1rem">
              <div class="stat-icon violet" style="width:2.5rem;height:2.5rem">
                <lucide-icon name="file-text" size="18"></lucide-icon>
              </div>
              <div>
                <p style="font-weight:600;font-size:.9rem;margin:0">Informe {{ informe.mes }}/{{ informe.anio }}</p>
                <p class="subtitle" style="margin:.2rem 0 0 0;font-size:.8rem">
                  Estado: {{ informe.estado }}
                </p>
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:.75rem">
              <span [ngClass]="informe.estado === 'APROBADO_COORDINADOR' ? 'aprobado' : 'pendiente'" class="status-pill">
                {{ informe.estado }}
              </span>
              <button class="btn-icon" title="Ver Chat" (click)="idAyudantiaSeleccionada = informe.idAyudantia">
                <lucide-icon name="message-square" size="16"></lucide-icon>
              </button>
            </div>
          </div>
        }
      </div>

    </div>
  `,
  styles: [`
    .gestion-container { padding: 1.5rem; }
    .informe-card { background: white; border: 1px solid #e5e7eb; border-radius: 0.75rem; padding: 1rem; display: flex; justify-content: space-between; align-items: center; }
    .status-pill { padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }
    .status-pill.aprobado { background: #dcfce7; color: #166534; }
    .status-pill.pendiente { background: #fef9c3; color: #854d0e; }
    .btn-icon { background: #f3f4f6; border: none; border-radius: 0.5rem; padding: 0.5rem; cursor: pointer; color: #4b5563; }
    .btn-icon:hover { background: #e5e7eb; color: #1f2937; }
  `]
})
export class InformesComponent implements OnInit {
  private informeService = inject(InformeMensualService);
  private authService = inject(AuthService);
  informes: InformeMensual[] = [];
  idAyudantiaSeleccionada: number | null = null;

  ngOnInit() {
    const user = this.authService.getUser();
    if (user) {
      // Logic to fetch reports would go here
    }
  }
}
