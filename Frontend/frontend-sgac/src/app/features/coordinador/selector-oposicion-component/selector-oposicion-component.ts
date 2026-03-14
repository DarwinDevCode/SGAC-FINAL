// src/app/features/coordinador/selector-oposicion/selector-oposicion.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { Router }                    from '@angular/router';
import { LucideAngularModule }       from 'lucide-angular';

import { EvaluacionOposicionService } from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import { ConvocatoriaOposicionDTO } from '../../../core/models/evaluaciones/EvaluacionOposicion';

@Component({
  selector:    'app-selector-oposicion',
  standalone:  true,
  imports:     [CommonModule, LucideAngularModule],
  templateUrl: './selector-oposicion-component.html',
  styleUrls:   ['./selector-oposicion-component.css'],
})
export class SelectorOposicionComponent implements OnInit {

  private svc    = inject(EvaluacionOposicionService);
  private router = inject(Router);

  loading        = true;
  error          = '';
  convocatorias: ConvocatoriaOposicionDTO[] = [];

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;
    this.error   = '';

    this.svc.listarConvocatoriasAptas().subscribe({
      next: res => {
        this.loading = false;
        if (res.exito && res.datos?.length) {
          this.convocatorias = res.datos;
        } else {
          this.error = res.mensaje ?? 'No hay convocatorias disponibles.';
        }
      },
      error: (err: Error) => {
        this.loading = false;
        this.error   = err.message;
      },
    });
  }

  /** Navega hacia la gestión de temas y sorteo para una convocatoria específica */
  seleccionar(conv: ConvocatoriaOposicionDTO): void {
    this.router.navigate(['/coordinador/oposicion', conv.idConvocatoria]);
  }

  /** Etiqueta e icono del estado del proceso de oposición de una convocatoria */
  estadoProceso(conv: ConvocatoriaOposicionDTO): { label: string; icon: string; cls: string } {
    if (conv.tieneSorteo)   return { label: 'Sorteo realizado', icon: 'CalendarCheck2', cls: 'badge-teal' };
    if (conv.tieneComision) return { label: 'Tribunal conformado', icon: 'Users',       cls: 'badge-blue' };
    return                         { label: 'Pendiente inicio',    icon: 'Clock',        cls: 'badge-amber' };
  }

  /** Iniciales del docente para el avatar */
  iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .slice(0, 2)
      .map(p => p.charAt(0).toUpperCase())
      .join('');
  }
}
