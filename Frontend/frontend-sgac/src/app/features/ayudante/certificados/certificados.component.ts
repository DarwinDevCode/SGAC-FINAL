import { Component } from '@angular/core';

@Component({
    selector: 'app-certificados',
    standalone: true,
    template: `
    <div class="space-y-6">
      <h1 class="text-2xl font-bold text-gray-900">Mis Certificados</h1>
      <p class="text-gray-600">Historial de periodos culminados satisfactoriamente con el visado decanal.</p>
      
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mt-4">
        <!-- Tarjeta de Certificado -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex flex-col group hover:shadow-md transition-all">
          <div class="h-32 bg-gray-100 flex items-center justify-center border-b group-hover:bg-blue-50 transition-colors">
             <i class="fi fi-rr-diploma text-5xl text-gray-400 group-hover:text-blue-500"></i>
          </div>
          <div class="p-4 flex-1">
            <h3 class="font-bold text-lg text-gray-800">Ayudante Laboratorio (Física I)</h3>
            <p class="text-sm text-gray-500 mt-1">Periodo Técnico 2026-II</p>
          </div>
          <div class="p-4 bg-gray-50 border-t flex justify-between items-center">
            <span class="text-xs font-semibold text-green-700 bg-green-100 px-2 py-1 rounded">Visado Oficial</span>
            <button class="text-primary hover:text-primary-700 font-medium text-sm flex items-center gap-1">
              Descargar PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class CertificadosComponent { }
