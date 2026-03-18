// src/app/features/coordinador/gestion-oposicion-component/gestion-oposicion-component.ts
import {
  Component, OnInit, OnDestroy, inject
} from '@angular/core';
import { CommonModule }          from '@angular/common';
import { FormsModule }           from '@angular/forms';
import { LucideAngularModule }   from 'lucide-angular';
import {ActivatedRoute, Router} from '@angular/router';   // ← FIX: necesario para leer el ID de la URL

import { EvaluacionOposicionService }    from '../../../core/services/evaluaciones/evaluacion-oposicion-service';
import {
  TurnoOposicion,
  TemaOposicion,
  OposicionResponse
} from '../../../core/models/evaluaciones/EvaluacionOposicion';

type TabCoord = 'temas' | 'cronograma';

@Component({
  selector: 'app-gestion-oposicion',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gestion-oposicion-component.html',
  styleUrls: ['./gestion-oposicion-component.css']
})
export class GestionOposicionComponent implements OnInit, OnDestroy {
  // ActivatedRoute permite leer los parámetros de la URL de forma reactiva.
  // La ruta configurada es /coordinador/oposicion/:idConvocatoria, por lo que
  // el parámetro se llama 'idConvocatoria'.
  private route   = inject(ActivatedRoute);
  private svc     = inject(EvaluacionOposicionService);
  private router = inject(Router);



  loading     = false;
  loadingMsg  = '';
  toastMsg    = '';
  toastTipo   = 'ok';
  private toastTimer: any;

  // ── FIX: el ID de convocatoria siempre viene de la URL ────────────
  // Se inicializa en 0; ngOnInit lo sobreescribe con el valor real.
  // Si el componente se usa sin parámetro de ruta (improbable pero posible),
  // los métodos de carga detectarán id = 0 y el backend retornará error controlado.
  idConvocatoria = 0;
  tabActiva: TabCoord = 'temas';

  temas:           TemaOposicion[] = [];
  nuevoTema        = '';
  totalAptos       = 0;
  listoParaSorteo  = false;

  cronograma:        TurnoOposicion[] = [];
  turnoActivo:       TurnoOposicion | null = null;
  mostrarModalSorteo = false;
  sorteando          = false;
  sorteoFecha        = '';
  sorteoHora         = '';
  sorteoLugar        = '';
  cronogramaGenerado: TurnoOposicion[] = [];

  mostrarModalConfirm = false;
  confirmMsg          = '';
  confirmAccion: (() => void) | null = null;

  // ── Ciclo de vida ─────────────────────────────────────────────────

  /*
  ngOnInit(): void {
    // Leemos el parámetro de la URL sincrónicamente (snapshot es suficiente
    // porque el ID no cambia durante la vida del componente).
    const paramId = this.route.snapshot.paramMap.get('idConvocatoria');
    this.idConvocatoria = paramId ? Number(paramId) : 0;
    this.cargarTemas();

    console.log("ID DE LA CONVOCATORIA: " +  this.idConvocatoria)
  }
   */

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('idConvocatoria');

      if (!id || id === '0') {
        console.error("Acceso denegado: No se proporcionó un ID de convocatoria válido.");
        this.toast("Por favor, selecciona una convocatoria de la lista.", "warn");
        this.router.navigate(['/coordinador/convocatorias']);
        return;
      }
      this.idConvocatoria = Number(id);
      console.log("=== GESTIONANDO CONVOCATORIA ID:", this.idConvocatoria, "===");
      this.cargarTemas();
    });
  }


  ngOnDestroy(): void {
    clearTimeout(this.toastTimer);
  }

  // ── Navegación entre pestañas ─────────────────────────────────────

  cambiarTab(tab: TabCoord): void {
    this.tabActiva = tab;
    tab === 'cronograma' ? this.cargarCronograma() : this.cargarTemas();
  }

  cargarTemas(): void {
    this.loading    = true;
    this.loadingMsg = 'Cargando temas...';

    this.svc.listarTemas(this.idConvocatoria).subscribe({
      next: res => {
        console.log("Listo para sorteo:  ", res.listoParaSorteo);



        this.loading         = false;
        this.temas           = res.temas ?? [];
        this.totalAptos      = res.totalAptos      ?? 0;
        this.listoParaSorteo = res.listoParaSorteo ?? false;
      },
      error: (err) => {
        console.error('=== ERROR COMPLETO ===', err);
        this.loading = false;
        this.toast(err.message, 'err');
      }
    });
  }

  agregarTema(): void {
    const txt = this.nuevoTema.trim();
    if (!txt) return;
    this.loading = true; this.loadingMsg = 'Guardando tema...';
    this.svc.registrarTemas(this.idConvocatoria, [{ descripcionTema: txt }]).subscribe({
      next: res => {
        this.loading         = false;
        this.nuevoTema       = '';
        this.totalAptos      = res.totalAptos      ?? this.totalAptos;
        this.listoParaSorteo = res.listoParaSorteo ?? false;
        this.toast(res.mensaje ?? 'Tema agregado.', 'ok');
        this.cargarTemas();
      },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); }
    });
  }

  confirmarLimpiar(): void {
    this.confirmar(
      '¿Limpiar todo el banco de temas? Esta acción no se puede deshacer si no hay evaluaciones iniciadas.',
      () => this.limpiarBanco()
    );
  }

  private limpiarBanco(): void {
    this.loading = true;
    this.svc.limpiarBanco(this.idConvocatoria).subscribe({
      next: res => { this.loading = false; this.toast(res.mensaje ?? 'Banco limpiado.', 'ok'); this.cargarTemas(); },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); }
    });
  }

  // ══════════════════════════════════════════════════════════════════
  // CRONOGRAMA Y SORTEO
  // ══════════════════════════════════════════════════════════════════

  cargarCronograma(): void {
    this.loading = true; this.loadingMsg = 'Cargando cronograma...';
    this.svc.obtenerCronograma(this.idConvocatoria).subscribe({
      next: res => { this.loading = false; this.cronograma = res.cronograma ?? []; },
      error: (err: Error) => { this.loading = false; this.toast(err.message, 'err'); }
    });
  }

  abrirModalSorteo(): void {
    this.sorteoFecha  = '';
    this.sorteoHora   = '';
    this.sorteoLugar  = '';
    this.cronogramaGenerado = [];
    this.mostrarModalSorteo = true;
  }

  ejecutarSorteo(): void {
    if (!this.sorteoFecha || !this.sorteoHora || !this.sorteoLugar) {
      this.toast('Completa todos los campos del sorteo.', 'warn'); return;
    }
    this.sorteando = true;
    this.svc.ejecutarSorteo({
      idConvocatoria: this.idConvocatoria,
      fecha:          this.sorteoFecha,
      horaInicio:     this.sorteoHora,
      lugar:          this.sorteoLugar
    }).subscribe({
      next: res => {
        this.sorteando          = false;
        this.cronogramaGenerado = res.cronograma ?? [];
        this.toast(`Sorteo completado. ${res.turnos} turno(s) programado(s).`, 'ok');
        this.cargarCronograma();
      },
      error: (err: Error) => { this.sorteando = false; this.toast(err.message, 'err'); }
    });
  }

  cerrarModalSorteo(): void {
    if (!this.sorteando) this.mostrarModalSorteo = false;
  }

  iniciarTurno(turno: TurnoOposicion): void {
    this.confirmar(`¿Iniciar la evaluación de ${turno.nombres} ${turno.apellidos}?`, () => {
      this.svc.iniciarEvaluacion(turno.idEvaluacionOposicion).subscribe({
        next: res => { this.toast(res.mensaje ?? 'Evaluación iniciada.', 'ok'); this.cargarCronograma(); },
        error: (err: Error) => this.toast(err.message, 'err')
      });
    });
  }

  marcarNoPresento(turno: TurnoOposicion): void {
    this.confirmar(`¿Marcar a ${turno.nombres} ${turno.apellidos} como No Presentó?`, () => {
      this.svc.marcarNoPresento(turno.idEvaluacionOposicion).subscribe({
        next: res => { this.toast(res.mensaje ?? 'Marcado.', 'ok'); this.cargarCronograma(); },
        error: (err: Error) => this.toast(err.message, 'err')
      });
    });
  }

  finalizarTurno(turno: TurnoOposicion): void {
    this.confirmar(
      `¿Finalizar y cerrar el acta de ${turno.nombres} ${turno.apellidos}? Esta acción es irreversible.`,
      () => {
        this.svc.finalizarEvaluacion(turno.idEvaluacionOposicion).subscribe({
          next: res => { this.toast(`Acta cerrada. Nota final: ${res.puntajeFinal}`, 'ok'); this.cargarCronograma(); },
          error: (err: Error) => this.toast(err.message, 'err')
        });
      }
    );
  }

  // ── Helpers de UI ─────────────────────────────────────────────────

  badgeEstado(estado: string): string {
    const m: Record<string, string> = {
      PROGRAMADA: 'badge-blue', EN_CURSO: 'badge-amber',
      FINALIZADA: 'badge-green', NO_PRESENTO: 'badge-red'
    };
    return m[estado] ?? 'badge-gray';
  }

  formatHora(h?: string): string  { return h ?? '—'; }

  formatFecha(f?: string): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  get temasRestantes(): number {
    return Math.max(0, this.totalAptos - this.temas.length);
  }

  private confirmar(msg: string, accion: () => void): void {
    this.confirmMsg    = msg;
    this.confirmAccion = accion;
    this.mostrarModalConfirm = true;
  }

  aceptarConfirm(): void {
    this.mostrarModalConfirm = false;
    if (this.confirmAccion) { this.confirmAccion(); this.confirmAccion = null; }
  }

  cancelarConfirm(): void {
    this.mostrarModalConfirm = false;
    this.confirmAccion = null;
  }

  private toast(msg: string, tipo: 'ok' | 'err' | 'warn'): void {
    clearTimeout(this.toastTimer);
    this.toastMsg  = msg;
    this.toastTipo = tipo;
    this.toastTimer = setTimeout(() => this.toastMsg = '', tipo === 'err' ? 9000 : 4000);
  }
}
