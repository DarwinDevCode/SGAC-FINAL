import { Component } from '@angular/core';

@Component({
    selector: 'app-sesiones',
    standalone: true,
    template: `
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">Mis Sesiones Ejecutadas</h1>
        <button class="px-4 py-2 bg-primary text-white text-sm font-medium rounded shadow-sm hover:bg-primary-600">
          Registrar Ejecución
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white rounded-lg shadow-sm border border-gray-100 p-6 flex flex-col pt-4">
          <div class="flex justify-between">
            <h3 class="font-semibold text-lg">Sesión: Calificación de Prácticas 1</h3>
            <span class="text-sm text-gray-500">Hace 2 días</span>
          </div>
          <p class="text-sm text-gray-600 mt-2">Actividad asociada: Calificación de Prácticas 1</p>
          <div class="mt-4 bg-gray-50 border p-3 rounded text-sm flex items-center justify-between">
            <span class="font-medium text-gray-700">Evidencia (1 archivo)</span>
            <a href="#" class="text-primary hover:text-primary-600">Ver foto_laboratorio.jpg</a>
          </div>
        </div>
      </div>
    </div>
  `
})
export class SesionesComponent { }
