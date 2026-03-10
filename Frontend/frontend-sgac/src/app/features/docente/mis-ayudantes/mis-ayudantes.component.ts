import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
  LucideAngularModule,
  LUCIDE_ICONS,
  LucideIconProvider,
  Users,
  ClipboardList,
  CheckCircle,
  AlertTriangle,
  Eye,
  Download,
  Loader2,
  MousePointerClick,
  Info,
  FileText,
  RefreshCw,
} from 'lucide-angular';
import { SupervisionService } from '../../../core/services/supervision.service';
import {
  Ayudante,
  RegistroActividad,
  Evidencia,
  EvaluacionActividadRequest,
  EvaluacionEvidenciaRequest,
} from './ayudantes.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-mis-ayudantes',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, RouterModule, LucideAngularModule],
  templateUrl: './mis-ayudantes.component.html',
  styleUrl: './mis-ayudantes.component.css',
  providers: [
    {
      provide: LUCIDE_ICONS,
      multi: true,
      useValue: new LucideIconProvider({
        Users,
        ClipboardList,
        CheckCircle,
        AlertTriangle,
        Eye,
        Download,
        Loader2,
        MousePointerClick,
        Info,
        FileText,
        RefreshCw,
      }),
    },
  ],
})
export class MisAyudantesComponent implements OnInit {
  private readonly supervision = inject(SupervisionService);

  ayudantes: Ayudante[] = [];
  actividades: RegistroActividad[] = [];

  ayudanteSeleccionado: Ayudante | null = null;
  actividadSeleccionada: RegistroActividad | null = null;

  isLoadingAyudantes = false;
  isLoadingActividades = false;
  errorMessage: string | null = null;

  mensajeExito: string | null = null;

  ngOnInit() {
    this.cargarAyudantes();
  }

  cargarAyudantes() {
    this.isLoadingAyudantes = true;
    this.errorMessage = null;

    this.supervision
      .getMisAyudantes()
      .pipe(finalize(() => (this.isLoadingAyudantes = false)))
      .subscribe({
        next: (data) => {
          this.ayudantes = data || [];
          if (this.ayudantes.length > 0) {
            this.seleccionarAyudante(this.ayudantes[0]);
          }
        },
        error: (err: Error) => {
          this.errorMessage = err.message;
        },
      });
  }

  seleccionarAyudante(a: Ayudante) {
    this.ayudanteSeleccionado = a;
    this.actividadSeleccionada = null;
    this.actividades = [];
    this.cargarActividades(a.idAyudantia);
  }

  cargarActividades(idAyudantia: number) {
    this.isLoadingActividades = true;
    this.errorMessage = null;

    this.supervision
      .getActividadesAyudante(idAyudantia)
      .pipe(finalize(() => (this.isLoadingActividades = false)))
      .subscribe({
        next: (data) => {
          this.actividades = (data || []).map((x) => {
            // inicializar campos locales
            return {
              ...x,
              _estadoSeleccionado: x.idTipoEstadoRegistro ?? null,
              _observacionInput: x.observaciones ?? '',
              evidencias: (x.evidencias || []).map((ev) => ({
                ...ev,
                _estadoSeleccionado: ev.idTipoEstadoEvidencia ?? null,
                _observacionInput: ev.observaciones ?? '',
              })),
            };
          });

          if (this.actividades.length > 0) {
            this.seleccionarActividad(this.actividades[0]);
          }
        },
        error: (err: Error) => {
          this.errorMessage = err.message;
        },
      });
  }

  seleccionarActividad(a: RegistroActividad) {
    this.actividadSeleccionada = a;
  }

  // ----- Badges / estados -----

  statusClass(nombreEstado?: string | null) {
    const e = (nombreEstado || '').toUpperCase();

    if (e.includes('APROB')) return 'status-aprobado';
    if (e.includes('OBSERV')) return 'status-observado';
    if (e.includes('RECHAZ')) return 'status-rechazado';
    if (e.includes('PEND')) return 'status-pendiente';

    // evidencias: SUBIDO/REVISADO
    if (e.includes('SUBID')) return 'status-pendiente';
    if (e.includes('REVIS')) return 'status-pendiente';

    return 'status-pendiente';
  }

  nombreCompleto(a: Ayudante) {
    return `${a.nombresAyudante} ${a.apellidosAyudante}`.trim();
  }

  // ----- Evaluación Actividad -----

  puedeGuardarActividad(a: RegistroActividad) {
    const actual = a.idTipoEstadoRegistro ?? null;
    const nuevo = a._estadoSeleccionado ?? null;
    return nuevo != null && nuevo !== actual;
  }

  guardarEvaluacionActividad(a: RegistroActividad) {
    if (!a._estadoSeleccionado) return;

    const payload: EvaluacionActividadRequest = {
      idTipoEstadoRegistro: a._estadoSeleccionado,
      observaciones: (a._observacionInput || '').trim() || null,
    };

    this.mensajeExito = null;
    this.supervision.evaluarActividad(a.idRegistroActividad, payload).subscribe({
      next: () => {
        // reflejar cambios en UI
        a.idTipoEstadoRegistro = payload.idTipoEstadoRegistro;
        a.observaciones = payload.observaciones || null;
        a.fechaObservacion = new Date().toISOString().slice(0, 10);

        // toast
        if (payload.idTipoEstadoRegistro === 3) {
          this.toast('Evaluación guardada. Se notificó al ayudante (plazo de 24h iniciado).');
        } else {
          this.toast('Evaluación guardada correctamente.');
        }
      },
      error: (err: Error) => {
        this.errorMessage = err.message;
      },
    });
  }

  // ----- Evaluación Evidencia -----

  puedeGuardarEvidencia(ev: Evidencia) {
    const actual = ev.idTipoEstadoEvidencia ?? null;
    const nuevo = ev._estadoSeleccionado ?? null;
    return nuevo != null && nuevo !== actual;
  }

  guardarEvaluacionEvidencia(ev: Evidencia) {
    if (!ev._estadoSeleccionado) return;

    const payload: EvaluacionEvidenciaRequest = {
      idTipoEstadoEvidencia: ev._estadoSeleccionado,
      observaciones: (ev._observacionInput || '').trim() || null,
    };

    this.mensajeExito = null;
    this.supervision.evaluarEvidencia(ev.idEvidencia, payload).subscribe({
      next: () => {
        ev.idTipoEstadoEvidencia = payload.idTipoEstadoEvidencia;
        ev.observaciones = payload.observaciones || null;
        ev.fechaObservacion = new Date().toISOString().slice(0, 10);

        if (payload.idTipoEstadoEvidencia === 5) {
          this.toast('Evidencia observada. Se notificó al ayudante (plazo de 24h iniciado).');
        } else {
          this.toast('Evidencia evaluada correctamente.');
        }
      },
      error: (err: Error) => {
        this.errorMessage = err.message;
      },
    });
  }

  verEvidencia(ev: Evidencia) {
    this.supervision.descargarEvidencia(ev.rutaArchivo, ev.idEvidencia);
  }

  // ----- Toast -----

  toast(msg: string) {
    this.mensajeExito = msg;
    window.clearTimeout((this as any)._toastTimer);
    (this as any)._toastTimer = window.setTimeout(() => {
      this.mensajeExito = null;
    }, 2600);
  }

  trackByAyudantia(_i: number, a: Ayudante) {
    return a.idAyudantia;
  }

  trackByActividad(_i: number, a: RegistroActividad) {
    return a.idRegistroActividad;
  }

  trackByEvidencia(_i: number, e: Evidencia) {
    return e.idEvidencia;
  }
}
