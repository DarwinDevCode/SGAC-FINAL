import { Component } from '@angular/core';

@Component({
  selector: 'app-student',
  standalone: true,
  template: `
    <div class="dashboard-layout">
      <aside class="sidebar">
        <h2>PORTAL ESTUDIANTE</h2>
        <ul class="nav-list">
          <li class="nav-item active">Mis Postulaciones</li>
          <li class="nav-item">Convocatorias Abiertas</li>
          <li class="nav-item">Mis Horas</li>
        </ul>
        <button class="logout-btn" (click)="exit()">Cerrar Sesión</button>
      </aside>
      <main class="main-content">
        <h1>Bienvenido, Estudiante</h1>
        <div class="card-grid">
          <div class="stat-card"><h3>Estado de Postulación</h3><p>En Revisión</p></div>
          <div class="stat-card"><h3>Promedio General</h3><p>8.75 / 10</p></div>
        </div>
      </main>
    </div>
  `
})
export class StudentComponent
{ exit() { localStorage.clear(); window.location.href='/login'; } }

