import { Component } from '@angular/core';

@Component({
    selector: 'app-aprobaciones',
    standalone: true,
    template: `
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">Bandeja de Firma/Aprobaciones</h1>
      </div>

      <div class="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        <div class="p-4 bg-yellow-50 border-b border-yellow-100 flex items-center justify-between text-yellow-800">
          <span class="text-sm font-medium"><i class="fi fi-rr-info"></i> Tienes 1 documento(s) pendiente(s) de autorización legal.</span>
          <button class="bg-yellow-600 text-white px-3 py-1 text-xs font-semibold rounded hover:bg-yellow-700">Firmar Todos</button>
        </div>
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Referencia</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo Documento</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha Generación</th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Autorizar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 bg-white">
            <tr>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm font-medium text-gray-900">RES-FIN-2026-F1</div>
                <div class="text-xs text-gray-500">Postulación Ayudante F. Ciencias</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span class="inline-flex items-center gap-1"><i class="fi fi-rr-document text-red-500"></i> Acta de Resultados (PDF)</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                21 Oct, 2026
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <button class="text-green-600 hover:text-green-900 border border-green-600 hover:bg-green-50 px-3 py-1 rounded text-xs font-semibold transition-colors">
                  <i class="fi fi-rr-check"></i> Autorizar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Bitácora -->
      <div class="mt-8">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">Últimas autorizaciones (Auditoría)</h3>
        <div class="bg-white p-4 rounded border text-sm text-gray-600">
          <div class="flex justify-between py-2 border-b last:border-0">
             <span>CERT-2026-001 (Certificado de Ayudantía)</span>
             <span class="text-gray-400">Autorizado hace 3 días por jdecano</span>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AprobacionesComponent { }
