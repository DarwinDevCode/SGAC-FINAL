import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-docente',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-layout">
      <aside class="sidebar" style="background: #2E7D32;">
        <h2>GESTIÓN DOCENTE</h2>
        <ul class="nav-list">
          <li class="nav-item active">Mis Ayudantes</li>
          <li class="nav-item">Solicitar Ayudantía</li>
          <li class="nav-item">Revisar Informes</li>
          <li class="nav-item">Configuración</li>
        </ul>
        <button class="logout-btn" (click)="logout()">Cerrar Sesión</button>
      </aside>

      <main class="main-content">
        <header class="content-header">
          <h1>Panel del Docente</h1>
          <p>Supervisión y control de ayudantías asignadas.</p>
        </header>

        <div class="card-grid">
          <div class="stat-card">
            <h3>Ayudantes a Cargo</h3>
            <p class="stat-number">3</p>
          </div>
          <div class="stat-card">
            <h3>Informes Pendientes</h3>
            <p class="stat-number">2</p>
          </div>
          <div class="stat-card">
            <h3>Horas Totales Mes</h3>
            <p class="stat-number">45h</p>
          </div>
        </div>

        <section class="recent-activity">
          <h2>Actividad Reciente</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Ayudante</th>
                  <th>Asignatura</th>
                  <th>Estado Informe</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Sánchez Darwin</td>
                  <td>Modelos Matemáticos</td>
                  <td><span class="badge warning">Pendiente</span></td>
                </tr>
                <tr>
                  <td>García Juan</td>
                  <td>Arquitectura Web</td>
                  <td><span class="badge success">Aprobado</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  `,
  styles: [`
    .stat-number { font-size: 2.5rem; font-weight: bold; color: #1B5E20; margin: 10px 0; }
    .content-header { margin-bottom: 30px; }
    .recent-activity { margin-top: 40px; }
    .table-container { background: white; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; }
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 15px; text-align: left; border-bottom: 1px solid #f1f5f9; }
    th { background: #f8fafc; font-size: 0.8rem; color: #64748b; text-transform: uppercase; }
    .badge { padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: bold; }
    .success { background: #dcfce7; color: #166534; }
    .warning { background: #fef9c3; color: #854d0e; }
  `]
})
export class DocenteComponent {
  private router = inject(Router);

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
