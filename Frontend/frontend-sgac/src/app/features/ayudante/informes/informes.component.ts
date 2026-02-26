import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

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
        @for (informe of informes; track informe.periodo) {
          <div class="informe-card">
            <div style="display:flex;align-items:center;gap:1rem">
              <div class="stat-icon violet" style="width:2.5rem;height:2.5rem">
                <lucide-icon name="file-text" size="18"></lucide-icon>
              </div>
              <div>
                <p style="font-weight:600;font-size:.9rem;margin:0">Informe {{ informe.periodo }}</p>
                <p class="subtitle" style="margin:.2rem 0 0 0;font-size:.8rem">
                  {{ informe.actividades }} actividades · {{ informe.horas }} horas
                </p>
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:.75rem">
              <span [ngClass]="informe.estado === 'Aprobado' ? 'aprobado' : 'pendiente'" class="status-pill">
                {{ informe.estado }}
              </span>
              <button class="btn-icon" title="Descargar PDF">
                <lucide-icon name="download" size="16"></lucide-icon>
              </button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: []
})
export class InformesComponent {
  informes = [
    { periodo: 'Enero 2025', actividades: 8, horas: 20, estado: 'Aprobado' },
    { periodo: 'Diciembre 2024', actividades: 6, horas: 16, estado: 'Aprobado' },
    { periodo: 'Noviembre 2024', actividades: 4, horas: 10, estado: 'Pendiente' },
  ];
}
