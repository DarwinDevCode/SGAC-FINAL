// src/app/features/coordinador/gestion-oposicion-component/gestion-oposicion-component.ts
import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }                    from '@angular/common';
import { FormsModule }                     from '@angular/forms';
import { Router, ActivatedRoute }          from '@angular/router';
import { LucideAngularModule }             from 'lucide-angular';

import { EvaluacionOposicionService }    from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import {
  TurnoOposicion,
  TemaOposicion,
} from '../../../core/models/evaluaciones/EvaluacionOposicion';

type TabCoord = 'temas' | 'cronograma';

@Component({
  selector:    'app-gestion-oposicion',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-oposicion-component.html',
  styleUrls:   ['./gestion-oposicion-component.css'],
})
export class GestionOposicionComponent implements OnInit, OnDestroy {

  private route  = inject(ActivatedRoute);
  private router = inject(Router);
  private svc    = inject(EvaluacionOposicionService);

  // ── Contexto de la convocatoria (inyectado por la ruta) ───
  idConvocatoria = 0;
  tabActiva: TabCoord = 'temas';

  // ── Estado general ────────────────────────────────────────
  loading    = false;
  loadingMsg = '';
  toastMsg   = '';
  toastTipo  = 'ok';
  private toastTimer: any;

  // ── Temas ─────────────────────────────────────────────────
  temas:           TemaOposicion[] = [];
  nuevoTema        = '';
  totalAptos       = 0;
  listoParaSorteo  = false;

  // ── Cronograma ────────────────────────────────────────────
  cronograma: TurnoOposicion[] = [];

  // ── Modal sorteo ──────────────────────────────────────────
  mostrarModalSorteo  = false;
  sorteando           = false;
  sorteoFecha         = '';
  sorteoHora          = '';
  sorteoLugar         = '';
  cronogramaGenerado: TurnoOposicion[] = [];

  // ── Modal confirmación ────────────────────────────────────
  mostrarModalConfirm   = false;
  confirmMsg            = '';
  confirmAccion: (() => void) | null = null;

  // ═══════════════════════════════════════════════════════════
  ngOnInit(): void {
    // El idConvocatoria llega siempre desde la URL porque la ruta
    // /coordinador/oposicion (sin :id) carga SelectorOposicionComponent.
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('idConvocatoria'));
      if (!id) {
        // Defensa ante navegación directa sin ID
        this.router.navigate(['/coordinador/oposicion']);
        return;
      }
      this.idConvocatoria = id;
      this.cargarTemas();
    });
  }

  ngOnDestroy(): void { clearTimeout(this.toastTimer); }

  /** Regresa al selector de convocatorias */
  volver(): void {
    this.router.navigate(['/coordinador/oposicion']);
  }

  cambiarTab(tab: TabCoord): void {
    this.tabActiva = tab;
    tab === 'cronograma' ? this.cargarCronograma() : this.cargarTemas();
  }

  // ══════════════════════════════════════════════════════════
  // BANCO DE TEMAS
  // ══════════════════════════════════════════════════════════

  cargarTemas(): void {
    this.loading    = true;
    this.loadingMsg = 'Cargando temas...';
    this.svc.listarTemas(this.idConvocatoria).subscribe({
      next: res => {
        this.loading         = false;
        this.temas           = res.temas ?? [];
        this.totalAptos      = res.totalAptos ?? 0;
        this.listoParaSorteo = res.listoParaSorteo ?? false;
      },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); },
    });
  }

  agregarTema(): void {
    const txt = this.nuevoTema.trim();
    if (!txt) return;
    this.loading    = true;
    this.loadingMsg = 'Guardando tema...';
    this.svc.registrarTemas(this.idConvocatoria, [{ descripcionTema: txt }]).subscribe({
      next: res => {
        this.loading         = false;
        this.nuevoTema       = '';
        this.totalAptos      = res.totalAptos    ?? this.totalAptos;
        this.listoParaSorteo = res.listoParaSorteo ?? false;
        this.toast(res.mensaje ?? 'Tema agregado.', 'ok');
        this.cargarTemas();
      },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); },
    });
  }

  confirmarLimpiar(): void {
    this.confirmar(
      '¿Limpiar todo el banco de temas? Esta acción no se puede deshacer si no hay evaluaciones iniciadas.',
      () => this.limpiarBanco(),
    );
  }

  private limpiarBanco(): void {
    this.loading = true;
    this.svc.limpiarBanco(this.idConvocatoria).subscribe({
      next: res => { this.loading = false; this.toast(res.mensaje ?? 'Banco limpiado.', 'ok'); this.cargarTemas(); },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); },
    });
  }

  // ══════════════════════════════════════════════════════════
  // CRONOGRAMA Y SORTEO
  // ══════════════════════════════════════════════════════════

  cargarCronograma(): void {
    this.loading    = true;
    this.loadingMsg = 'Cargando cronograma...';
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => { this.loading = false; this.cronograma = res.cronograma ?? []; },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); },
    });
  }

  abrirModalSorteo(): void {
    this.sorteoFecha = this.sorteoHora = this.sorteoLugar = '';
    this.cronogramaGenerado = [];
    this.mostrarModalSorteo = true;
  }

  ejecutarSorteo(): void {
    if (!this.sorteoFecha || !this.sorteoHora || !this.sorteoLugar) {
      this.toast('Completa todos los campos del sorteo.', 'warn');
      return;
    }
    this.sorteando = true;
    this.svc.ejecutarSorteo({
      idConvocatoria: this.idConvocatoria,
      fecha:          this.sorteoFecha,
      horaInicio:     this.sorteoHora,
      lugar:          this.sorteoLugar,
    }).subscribe({
      next: res => {
        this.sorteando          = false;
        this.cronogramaGenerado = res.cronograma ?? [];
        this.toast(`Sorteo completado. ${res.turnos} turno(s) programado(s).`, 'ok');
        this.cargarCronograma();
      },
      error: (err: Error) => { this.sorteando = false; this.toast(err.message, 'err'); },
    });
  }

  cerrarModalSorteo(): void { if (!this.sorteando) this.mostrarModalSorteo = false; }

  iniciarTurno(turno: TurnoOposicion): void {
    this.confirmar(`¿Iniciar la evaluación de ${turno.nombres} ${turno.apellidos}?`, () => {
      this.svc.iniciarEvaluacion(turno.idEvaluacionOposicion).subscribe({
        next: res => { this.toast(res.mensaje ?? 'Evaluación iniciada.', 'ok'); this.cargarCronograma(); },
        error: (err: Error) => this.toast(err.message, 'err'),
      });
    });
  }

  marcarNoPresento(turno: TurnoOposicion): void {
    this.confirmar(`¿Marcar a ${turno.nombres} ${turno.apellidos} como No Presentó?`, () => {
      this.svc.marcarNoPresento(turno.idEvaluacionOposicion).subscribe({
        next: res => { this.toast(res.mensaje ?? 'Marcado.', 'ok'); this.cargarCronograma(); },
        error: (err: Error) => this.toast(err.message, 'err'),
      });
    });
  }

  finalizarTurno(turno: TurnoOposicion): void {
    this.confirmar(
      `¿Finalizar y cerrar el acta de ${turno.nombres} ${turno.apellidos}? Esta acción es irreversible.`,
      () => {
        this.svc.finalizarEvaluacion(turno.idEvaluacionOposicion).subscribe({
          next: res => { this.toast(`Acta cerrada. Nota final: ${res.puntajeFinal}`, 'ok'); this.cargarCronograma(); },
          error: (err: Error) => this.toast(err.message, 'err'),
        });
      },
    );
  }

  // ── Helpers ──────────────────────────────────────────────

  get temasRestantes(): number { return Math.max(0, this.totalAptos - this.temas.length); }

  badgeEstado(e: string): string {
    const m: Record<string, string> = {
      PROGRAMADA: 'badge-blue', EN_CURSO: 'badge-amber',
      FINALIZADA: 'badge-green', NO_PRESENTO: 'badge-red',
    };
    return m[e] ?? 'badge-gray';
  }

  formatHora(h?: string): string { return h ?? '—'; }
  formatFecha(f?: string): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  private confirmar(msg: string, accion: () => void): void {
    this.confirmMsg = msg; this.confirmAccion = accion; this.mostrarModalConfirm = true;
  }

  aceptarConfirm(): void {
    this.mostrarModalConfirm = false;
    if (this.confirmAccion) { this.confirmAccion(); this.confirmAccion = null; }
  }

  cancelarConfirm(): void { this.mostrarModalConfirm = false; this.confirmAccion = null; }

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg = msg; this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 9000 : 4000);
  }
}
