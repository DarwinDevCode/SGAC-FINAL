// finalizar-seleccion-btn.component.ts
import {
  Component, Input, Output, EventEmitter,
  inject, signal, OnDestroy, AfterViewInit,
  ViewChild, TemplateRef, EmbeddedViewRef,
  ViewContainerRef, Renderer2,
  ViewEncapsulation
} from '@angular/core';
import { DOCUMENT }           from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil, finalize } from 'rxjs';

import { FinalizarSeleccionService }
  from '../../../core/services/convocatorias/finalizar-seleccion-service';
import { FinalizarSeleccionResponse, ResumenSeleccion }
  from '../../../core/models/convocatoria/finalizar-seleccion';

@Component({
  selector:    'app-finalizar-seleccion-btn',
  standalone:  true,
  imports:     [LucideAngularModule],
  templateUrl: './finalizar-seleccion-btn-component.html',
  styleUrl:    './finalizar-seleccion-btn-component.css',
  encapsulation: ViewEncapsulation.None,
})
export class FinalizarSeleccionBtnComponent implements AfterViewInit, OnDestroy {

  // ── Inputs / Outputs ──────────────────────────────────────────────
  @Input({ required: true }) idConvocatoria!:    number;
  @Input()                   nombreConvocatoria: string = '';
  @Output() seleccionFinalizada = new EventEmitter<ResumenSeleccion>();

  // ── Referencia al <ng-template> del modal en el HTML ─────────────
  @ViewChild('modalTemplate', { read: TemplateRef })
  private modalTemplate!: TemplateRef<any>;

  // ── Dependencias ──────────────────────────────────────────────────
  private svc      = inject(FinalizarSeleccionService);
  private vcr      = inject(ViewContainerRef);
  private renderer = inject(Renderer2);
  private document = inject(DOCUMENT);
  private destroy$ = new Subject<void>();

  // ── Estado interno ────────────────────────────────────────────────
  mostrarModal = signal(false);
  ejecutando   = signal(false);
  resultado    = signal<FinalizarSeleccionResponse | null>(null);
  errorMsg     = signal<string>('');

  // ── Nodos del portal (referencia para poder destruirlos) ──────────
  private portalHost?: HTMLElement;
  private portalView?: EmbeddedViewRef<any>;

  ngAfterViewInit(): void {
    // No hacemos nada aquí; solo confirma que @ViewChild ya está listo.
  }

  ngOnDestroy(): void {
    this._desmontarPortal();
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ══ API pública ═══════════════════════════════════════════════════

  abrirModal(): void {
    this.resultado.set(null);
    this.errorMsg.set('');
    this.mostrarModal.set(true);
    this._montarPortal();
  }

  cerrarModal(): void {
    if (this.ejecutando()) return;
    this.mostrarModal.set(false);
    this._desmontarPortal();
  }

  confirmarFinalizar(): void {
    this.ejecutando.set(true);
    this.errorMsg.set('');

    this.svc.finalizarSeleccion(this.idConvocatoria)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.ejecutando.set(false))
      )
      .subscribe({
        next: (res) => {
          this.resultado.set(res);
          if (res.exito && res.resumen) {
            this.seleccionFinalizada.emit(res.resumen);
          }
        },
        error: (err) => {
          this.errorMsg.set(
            err?.error?.message ?? 'Error inesperado al procesar la solicitud.'
          );
        }
      });
  }

  // ══ Portal ════════════════════════════════════════════════════════

  /**
   * Crea un nodo host en <body> y renderiza el template del modal
   * dentro de él. Al estar colgado directamente de <body>:
   *   · No hereda ningún transform de los ancestros del componente.
   *   · position:fixed funciona relativo al viewport real.
   *   · Los estilos globales (ViewEncapsulation.None) llegan sin problema.
   */
  private _montarPortal(): void {
    // 1. Crear el nodo contenedor en <body>
    this.portalHost = this.renderer.createElement('div') as HTMLElement;
    this.renderer.addClass(this.portalHost, 'fs-portal-host');
    this.renderer.appendChild(this.document.body, this.portalHost);

    // 2. Renderizar el <ng-template #modalTemplate> en ese contenedor
    this.portalView = this.vcr.createEmbeddedView(this.modalTemplate);
    // Forzar detección de cambios para que las señales reactivas se resuelvan
    this.portalView.detectChanges();
    // Mover los nodos raíz del template al host
    this.portalView.rootNodes.forEach(node =>
      this.renderer.appendChild(this.portalHost!, node)
    );
  }

  private _desmontarPortal(): void {
    if (this.portalView) {
      this.portalView.destroy();
      this.portalView = undefined;
    }
    if (this.portalHost?.parentNode) {
      this.renderer.removeChild(this.document.body, this.portalHost);
      this.portalHost = undefined;
    }
  }
}
