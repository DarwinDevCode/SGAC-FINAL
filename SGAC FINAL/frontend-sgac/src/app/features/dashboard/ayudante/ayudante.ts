import { Component } from '@angular/core';

// ayudante.component.ts
@Component({
  selector: 'app-ayudante-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-container">
      <aside class="sidebar" style="background: #2E7D32;"> <h2 class="logo">MI AYUDANTÍA</h2>
        <nav>
          <button class="nav-btn">Registrar Actividad</button>
          <button class="nav-btn">Mis Horas</button>
          <button class="nav-btn logout" (click)="logout()">Cerrar sesión</button>
        </nav>
      </aside>
      <main class="content">
        <h1>Panel del Ayudante</h1>
        <div class="card">
          <h3>Horas registradas este mes</h3>
          <p>32 / 40 horas</p>
        </div>
      </main>
    </div>
  `,
  styles: [/* Estilos base */]
})

export class AyudanteComponent {

  protected logout() {

  }
}
