// src/app/features/coordinador/selector-meritos-component/selector-meritos-component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { FormsModule }               from '@angular/forms';
import { Router }                    from '@angular/router';
import { LucideAngularModule }       from 'lucide-angular';

import { EvaluacionMeritosService } from '../../../core/services/evaluaciones/evaluacion-meritos-service';
import { PostulacionParaMeritosItem } from '../../../core/models/evaluaciones/Evaluacionmeritos';

@Component({
  selector:    'app-selector-meritos',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './selector-meritos-component.html',
  styleUrls:   ['./selector-meritos-component.css'],
})
export class SelectorMeritosComponent implements OnInit {

  private svc    = inject(EvaluacionMeritosService);
  private router = inject(Router);

  loading    = true;
  error      = '';
  faseActiva = false;
  busqueda   = '';

  postulaciones:  PostulacionParaMeritosItem[] = [];
  filtradas:      PostulacionParaMeritosItem[] = [];

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.error   = '';
    this.svc.listar().subscribe({
      next: res => {
        this.loading      = false;
        this.faseActiva   = res.faseActiva ?? false;
        if (!res.exito) { this.error = res.mensaje ?? 'Sin datos.'; return; }
        this.postulaciones = res.postulaciones ?? [];
        this.aplicarFiltro();
      },
      error: (err: Error) => { this.loading = false; this.error = err.message; },
    });
  }

  aplicarFiltro(): void {
    const b = this.busqueda.toLowerCase().trim();
    this.filtradas = !b
      ? [...this.postulaciones]
      : this.postulaciones.filter(p =>
        `${p.nombres} ${p.apellidos}`.toLowerCase().includes(b) ||
        p.nombreAsignatura.toLowerCase().includes(b) ||
        p.matricula.toLowerCase().includes(b)
      );
  }

  seleccionar(item: PostulacionParaMeritosItem): void {
    this.router.navigate(['/coordinador/evaluacion-meritos', item.idPostulacion]);
  }

  // ── Helpers de UI ─────────────────────────────────────────────

  get totalCalificados():  number { return this.postulaciones.filter(p => p.estadoEvaluacion).length; }
  get totalFinalizados():  number { return this.postulaciones.filter(p => p.estadoEvaluacion === 'FINALIZADA').length; }
  get totalPendientes():   number { return this.postulaciones.filter(p => !p.estadoEvaluacion).length; }

  claseEstado(item: PostulacionParaMeritosItem): string {
    if (item.estadoEvaluacion === 'FINALIZADA') return 'badge-green';
    if (item.estadoEvaluacion === 'BORRADOR')   return 'badge-amber';
    return 'badge-gray';
  }

  labelEstado(item: PostulacionParaMeritosItem): string {
    if (item.estadoEvaluacion === 'FINALIZADA') return 'Finalizada';
    if (item.estadoEvaluacion === 'BORRADOR')   return 'Borrador';
    return 'Pendiente';
  }

  iconoEstado(item: PostulacionParaMeritosItem): string {
    if (item.estadoEvaluacion === 'FINALIZADA') return 'shield-check';
    if (item.estadoEvaluacion === 'BORRADOR')   return 'pencil';
    return 'clock';
  }

  iniciales(nombres: string, apellidos: string): string {
    return `${nombres.charAt(0)}${apellidos.charAt(0)}`.toUpperCase();
  }
}
