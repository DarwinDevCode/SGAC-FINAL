import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
    selector: 'app-coordinador-resoluciones',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    templateUrl: './resoluciones.html',
    styleUrl: './resoluciones.css',
})
export class ResolucionesComponent {
    resoluciones = [
        { numero: 'RES-001-2025', descripcion: 'Designación Ayudante Cátedra - Bases de Datos', fecha: '2025-01-15', estado: 'Firmada' },
        { numero: 'RES-002-2025', descripcion: 'Designación Ayudante Cátedra - Programación I', fecha: '2025-01-20', estado: 'Firmada' },
        { numero: 'RES-003-2025', descripcion: 'Renovación Ayudantía - Matemática I', fecha: '2025-02-01', estado: 'Pendiente' },
    ];
}
