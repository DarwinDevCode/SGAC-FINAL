import { Component } from '@angular/core';

// decano.component.ts
@Component({
  selector: 'app-decano-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-container">
      <aside class="sidebar" style="background: #154318;"> <h2 class="logo">DECANATO</h2>
        <nav>
          <button class="nav-btn">Aprobaciones Finales</button>
          <button class="nav-btn">Estadísticas Facultad</button>
          <button class="nav-btn logout" (click)="logout()">Cerrar sesión</button>
        </nav>
      </aside>
      <main class="content">
        <h1>Bienvenido, Sr. Decano</h1>
        <div class="stats-grid">
          <div class="card"><h3>Total Ayudantes</h3><p>150</p></div>
          <div class="card"><h3>Presupuesto Ejecutado</h3><p>85%</p></div>
        </div>
      </main>
    </div>
  `,
  styles: [/* Estilos base */]
})

export class DecanoComponent {

  protected logout() {

  }
}
