import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  template: `
    <div class="flex flex-col items-center justify-center min-h-[60vh] text-center p-8">
      <div class="text-6xl mb-4">🔒</div>
      <h1 class="text-3xl font-bold text-gray-800 mb-2">Acceso Denegado</h1>
      <p class="text-gray-600 mb-6">No tienes los permisos necesarios para acceder a esta página.</p>
      <button (click)="goBack()" class="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition-colors">
        Volver
      </button>
    </div>
  `
})
export class ForbiddenComponent {
  constructor(private router: Router) {}
  
  goBack() {
    this.router.navigate(['/']); // Or location.back()
  }
}
