import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  template: `
    <h1> Hola </h1>
  `,
  styles: [`
    .dashboard-container { display: flex; min-height: 100vh; background: #f8fafc; font-family: sans-serif; }
    .sidebar { width: 260px; background: #1B5E20; color: white; padding: 2rem; display: flex; flex-direction: column; }
    .logo { font-size: 1.5rem; margin-bottom: 2rem; border-bottom: 1px solid rgba(255,255,255,0.2); padding-bottom: 1rem; }
    .nav-btn { background: none; border: none; color: white; padding: 1rem; text-align: left; cursor: pointer; width: 100%; border-radius: 8px; margin-bottom: 0.5rem; }
    .nav-btn:hover, .active { background: rgba(255,255,255,0.1); }
    .logout { margin-top: auto; color: #ff9999; }
    .content { flex: 1; padding: 2.5rem; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; margin-top: 2rem; }
    .card { background: white; padding: 1.5rem; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    h3 { margin-top: 0; color: #64748b; font-size: 0.875rem; text-transform: uppercase; }
    p { font-size: 1.5rem; font-weight: bold; margin: 0; }
  `]
})
export class AdminComponent {
  private router = inject(Router);

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}
