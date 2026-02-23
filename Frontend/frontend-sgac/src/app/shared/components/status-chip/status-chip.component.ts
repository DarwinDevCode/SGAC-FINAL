import { Component, Input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-status-chip',
    standalone: true,
    imports: [CommonModule],
    template: `
    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border"
          [ngClass]="colorClasses()">
      {{ label() }}
    </span>
  `
})
export class StatusChipComponent {
    @Input({ required: true }) status!: string;
    @Input() type: 'CONVOCATORIA' | 'POSTULACION' = 'CONVOCATORIA';

    label = computed(() => {
        return this.status.replace(/_/g, ' ');
    });

    colorClasses = computed(() => {
        switch (this.status) {
            // Convocatoria Statuses
            case 'BORRADOR':
                return 'bg-gray-100 text-gray-800 border-gray-200';
            case 'ABIERTA':
                return 'bg-green-100 text-green-800 border-green-200';
            case 'CERRADA':
                return 'bg-red-100 text-red-800 border-red-200';
            case 'EN_EVALUACIÓN': case 'EN_EVALUACION':
                return 'bg-blue-100 text-blue-800 border-blue-200';
            case 'LISTA_PARA_APROBAR':
                return 'bg-purple-100 text-purple-800 border-purple-200';
            case 'PUBLICADA':
                return 'bg-teal-100 text-teal-800 border-teal-200';

            // Postulacion Statuses
            case 'REGISTRADA':
                return 'bg-blue-50 text-blue-700 border-blue-200';
            case 'EN_REVISIÓN': case 'EN_REVISION':
                return 'bg-yellow-50 text-yellow-700 border-yellow-200';
            case 'OBSERVADA':
                return 'bg-orange-100 text-orange-800 border-orange-200';
            case 'VALIDADA':
                return 'bg-green-50 text-green-700 border-green-200';
            case 'RECHAZADA':
                return 'bg-red-50 text-red-700 border-red-200';
            case 'EVALUADA':
                return 'bg-indigo-50 text-indigo-700 border-indigo-200';
            case 'FINALIZADA':
                return 'bg-gray-50 text-gray-700 border-gray-200';

            default:
                return 'bg-gray-100 text-gray-800 border-gray-200';
        }
    });
}
