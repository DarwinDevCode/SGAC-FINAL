import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { Router }                    from '@angular/router';
import { LucideAngularModule }       from 'lucide-angular';
import { AuthService }               from '../../../core/services/auth-service';
import { ComisionService }           from '../../../core/services/convocatorias/comision-service';
import {
  ConvocatoriaComision,
  PostulanteComision,
  FaseEvaluacion
} from '../../../core/models/convocatoria/comision';
import {FaseInfo} from '../../../core/models/configuracion/Cronograma';
import {forkJoin, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {CronogramaActivoService} from '../../../core/services/configuracion/cronograma-activo-service';

@Component({
  selector: 'app-gestion-evaluaciones',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './gestion-evaluaciones.html',
  styleUrls: ['./gestion-evaluaciones.css']
})
export class GestionEvaluacionesComponent implements OnInit {
  private authSrv      = inject(AuthService);
  private comisionSrv  = inject(ComisionService);
  private cronogramaSrv = inject(CronogramaActivoService);
  private router       = inject(Router);

  // ── Estado general ────────────────────────────────────────
  loading  = true;
  errorMsg = '';
  rolUsuario = '';

  esCoordinador         = false;
  faseRequisitos: FaseInfo | null = null;
  puedeGenerarComisiones = false;       // fecha_fin de fase <= hoy
  generando             = false;
  resultadoGeneracion: {
    exito: boolean;
    mensaje: string;
    creadas: number;
    omitidas: number;
  } | null = null;

  // ── Panel de evaluaciones (DECANO / COORDINADOR / DOCENTE) ──
  convocatorias:    ConvocatoriaComision[] = [];
  convSeleccionada: ConvocatoriaComision | null = null;

  // ── Ciclo de vida ─────────────────────────────────────────
  ngOnInit(): void {
    const user = this.authSrv.getUser();
    if (!user) {
      this.errorMsg = 'No hay sesión activa.';
      this.loading  = false;
      return;
    }

    const rolMap: Record<string, string> = {
      DECANO: 'DECANO', COORDINADOR: 'COORDINADOR', DOCENTE: 'DOCENTE'
    };

    const rolBusqueda = user.rolActual?.toUpperCase() || '';
    this.rolUsuario = rolMap[rolBusqueda] ?? 'DOCENTE';
    this.esCoordinador = this.rolUsuario === 'COORDINADOR';

    // ── Llamadas en paralelo ──────────────────────────────────
    forkJoin({
      cronograma: this.cronogramaSrv.obtenerCronogramaActual().pipe(
        catchError(() => of(null))
      ),
      comisiones: this.comisionSrv.obtenerDetalle(user.idUsuario, this.rolUsuario).pipe(
        catchError((err: Error) => of({ exito: false, mensaje: err.message, rol: this.rolUsuario }))
      )
    }).subscribe(({ cronograma, comisiones }) => {
      this.loading = false;

      // Procesar cronograma → buscar fase Evaluación de Requisitos
      if (cronograma?.fases) {
        this.faseRequisitos = this.buscarFaseRequisitos(cronograma.fases);
        if (this.faseRequisitos) {
          this.puedeGenerarComisiones =
            new Date() >= new Date(this.faseRequisitos.fechaFin + 'T23:59:59');
        }
      }

      // Procesar comisiones asignadas
      if (comisiones && 'exito' in comisiones) {
        if (!comisiones.exito) {
          // Sin comisiones asignadas no es un error fatal — es información
          this.convocatorias = [];
        } else {
          this.convocatorias    = (comisiones as any).convocatorias ?? [];
          this.convSeleccionada = this.convocatorias[0] ?? null;
        }
      }
    });
  }

  // ── Generar comisiones ────────────────────────────────────
  generarComisiones(): void {
    if (!this.puedeGenerarComisiones || this.generando) return;

    this.generando         = true;
    this.resultadoGeneracion = null;

    this.comisionSrv.generarAutomatico().subscribe({
      next: res => {
        this.generando = false;
        this.resultadoGeneracion = {
          exito:    res.exito,
          mensaje:  res.mensaje,
          creadas:  res.comisionesCreadas ?? 0,
          omitidas: res.convocatoriasOmitidas ?? 0
        };
      },
      error: (err: Error) => {
        this.generando = false;
        this.resultadoGeneracion = {
          exito:    false,
          mensaje:  err.message,
          creadas:  0,
          omitidas: 0
        };
      }
    });
  }

  cerrarResultado(): void { this.resultadoGeneracion = null; }

  // ── Selección de convocatoria ─────────────────────────────
  seleccionar(conv: ConvocatoriaComision): void {
    this.convSeleccionada = conv;
  }

  iniciarEvaluacion(postulacion: PostulanteComision): void {
    this.router.navigate(['/evaluacion/calificar', postulacion.idPostulacion]);
  }

  // ── Helpers ───────────────────────────────────────────────
  formatFecha(f: string | null | undefined): string {
    if (!f) return '—';
    const [y, m, d] = f.split('-');
    return `${d}/${m}/${y}`;
  }

  faseActiva(fase: FaseEvaluacion | null): boolean {
    if (!fase) return false;
    const hoy    = new Date();
    const inicio = new Date(fase.fechaInicio + 'T00:00:00');
    const fin    = new Date(fase.fechaFin    + 'T23:59:59');
    return hoy >= inicio && hoy <= fin;
  }

  diasRestantes(fase: FaseEvaluacion | null): number {
    if (!fase) return 0;
    const fin = new Date(fase.fechaFin + 'T23:59:59');
    return Math.max(0, Math.ceil((fin.getTime() - Date.now()) / 86400000));
  }

  badgeEstado(codigo: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'badge-gray', APTO: 'badge-green', NO_APTO: 'badge-red',
      EN_EVALUACION: 'badge-blue', SELECCIONADO: 'badge-emerald'
    };
    return map[codigo?.toUpperCase()] ?? 'badge-gray';
  }

  labelEstado(estado: string): string {
    return estado?.replace(/_/g, ' ') ?? 'Pendiente';
  }

  /** Tooltip con motivo de deshabilitación del botón */
  tooltipBoton(): string {
    if (!this.faseRequisitos) {
      return 'No se encontró la fase de Evaluación de Requisitos en el cronograma.';
    }
    if (!this.puedeGenerarComisiones) {
      return `La fase "Evaluación de Requisitos" aún no ha finalizado. ` +
        `Cierra el ${this.formatFecha(this.faseRequisitos.fechaFin)}.`;
    }
    return 'Generar comisiones para todas las convocatorias con postulantes aptos.';
  }

  // ── Búsqueda flexible de la fase de requisitos ────────────
  private buscarFaseRequisitos(fases: FaseInfo[]): FaseInfo | null {
    const candidatos = fases.filter(f => {
      const n = f.nombre?.toLowerCase() ?? '';
      const c = f.codigo?.toLowerCase() ?? '';
      return n.includes('requisit') || c.includes('requisit') ||
        n.includes('eval_req') || c.includes('eval_req');
    });
    // Ordenar por orden ascendente y tomar el primero
    candidatos.sort((a, b) => a.orden - b.orden);
    return candidatos[0] ?? null;
  }
}
