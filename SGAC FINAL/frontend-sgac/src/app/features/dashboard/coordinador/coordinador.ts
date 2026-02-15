import { Component } from '@angular/core';

@Component({
  selector: 'app-coordinador-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-container">
      <aside class="sidebar">
        <h2 class="logo">SGAC COORD</h2>
        <nav>
          <button class="nav-btn active">Validaciones</button>
          <button class="nav-btn">Reportes Semanales</button>
          <button class="nav-btn logout" (click)="logout()">Salir</button>
        </nav>
      </aside>
      <main class="content">
        <h1>Gestión de Coordinación</h1>
        <div class="card">
          <h3>Solicitudes por Revisar</h3>
          <p>12 Ayudantías esperando firma</p>
        </div>
      </main>
    </div>
  `,
  styles: [/* Usar los mismos estilos del AdminComponent */]
})
export class CoordinadorComponent {

  protected logout() {

  }
}
