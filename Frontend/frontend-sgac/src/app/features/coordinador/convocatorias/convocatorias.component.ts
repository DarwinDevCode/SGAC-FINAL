import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription, forkJoin, catchError, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';


import { CoordinadorService } from '../../../core/services/coordinador-service';
import { PeriodoAcademicoService } from '../../../core/services/periodo-academico-service';
import { AuthService } from '../../../core/services/auth-service';
import { ConvocatoriaService } from '../../../core/services/convocatoria-service';
import { ConvocatoriaDTO } from '../../../core/dto/convocatoria';
import { PeriodoAcademicoDTO } from '../../../core/dto/periodo-academico';
import { AsignaturaComboDTO, DocenteComboDTO } from '../../../core/models/convocatoria.model';

const API_CONV = 'http://localhost:8080/api/convocatorias';

@Component({
  selector: 'app-coordinador-convocatorias-crud',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, LucideAngularModule],
  templateUrl: './convocatorias.html',
  styleUrl: './convocatorias.css',
})
export class CoordinadorConvocatoriasComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private coordinadorService = inject(CoordinadorService);
  private periodoService = inject(PeriodoAcademicoService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private subs = new Subscription();
  private convocatoriaService = inject(ConvocatoriaService);

  convocatorias: ConvocatoriaDTO[] = [];
  periodoActivo: PeriodoAcademicoDTO | null = null;

  docentes: DocenteComboDTO[] = [];
  asignaturas: AsignaturaComboDTO[] = [];

  periodosMap: Map<number, string> = new Map();

  loading = true;
  errorMensaje = '';
  successMensaje = '';
  textoBusqueda = '';
  idCarreraActual: number | null = null;

  mostrarModal = false;
  modoEdicion = false;
  loadingForm = false;
  convocatoriaEditId: number | null = null;

  form: FormGroup = this.fb.group({
    idAsignatura: [{ value: null, disabled: true }, Validators.required],
    idDocente: [null, Validators.required],
    idPeriodoAcademico: [{ value: null, disabled: true }],
    cuposDisponibles: [1, [Validators.required, Validators.min(1)]],
    fechaPublicacion: ['', Validators.required],
    fechaCierre: ['', Validators.required],
    estado: ['ABIERTA', Validators.required],
    activo: [true],
  });

  readonly estadoOpcion = ['ABIERTA', 'CERRADA', 'EN_EVALUACION', 'RESUELTA'];

  get convocatoriasFiltradas(): ConvocatoriaDTO[] {
    const t = this.textoBusqueda.toLowerCase().trim();
    if (!t) return this.convocatorias;
    return this.convocatorias.filter(c =>
      (c.nombreAsignatura || '').toLowerCase().includes(t) ||
      this.getPeriodoNombre(c.idPeriodoAcademico).toLowerCase().includes(t)
    );
  }

  getPeriodoNombre(id?: number): string {

    if (!id) return '—';
    return this.periodosMap.get(id) || String(id);
  }

  ngOnInit(): void {
    this.cargarDatos();
    this.loadDocentes();
  }
  ngOnDestroy(): void { this.subs.unsubscribe(); }

  cargarDatos() {
    this.loading = true;
    const user = this.authService.getUser();
    if (!user) { this.loading = false; return; }

    this.subs.add(
      forkJoin({
        coordinador: this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario),
        periodoActivo: this.periodoService.obtenerActivo().pipe(catchError((e) => { console.error('Error fetching active period:', e); return of(null); })),
      }).pipe(
        switchMap(({ coordinador, periodoActivo }) => {
          this.periodoActivo = periodoActivo;
          this.idCarreraActual = (coordinador as any).idCarrera ?? null;

          if (periodoActivo?.idPeriodoAcademico) {
            this.periodosMap.set(periodoActivo.idPeriodoAcademico, periodoActivo.nombrePeriodo);
            this.form.patchValue({ idPeriodoAcademico: periodoActivo.idPeriodoAcademico });
          }

          return forkJoin({
            convocatorias: this.coordinadorService.listarConvocatoriasPorCarrera(this.idCarreraActual ?? 0).pipe(catchError(() => of([]))),
          });
        })
      ).subscribe({
        next: ({ convocatorias }) => {
          this.convocatorias = convocatorias as ConvocatoriaDTO[];
          this.loading = false;
        },
        error: () => { this.errorMensaje = 'Error al cargar datos.'; this.loading = false; }
      })
    );

  }

  loadDocentes() {
    this.subs.add(
      this.convocatoriaService.getDocentesCarrera().pipe(catchError(() => of([]))).subscribe({
        next: (data) => {
          this.docentes = (data || []) as DocenteComboDTO[];
        },
        error: () => {
          this.docentes = [];
        }
      })
    );
  }

  onDocenteChange(event: any) {
    const rawValue = event?.target?.value;
    const idDocente = rawValue != null && rawValue !== '' ? Number(rawValue) : null;

    // limpiar asignaturas y seleccionar docente
    this.form.patchValue({ idDocente, idAsignatura: null });
    this.asignaturas = [];

    if (!idDocente) {
      this.form.get('idAsignatura')?.disable();
      return;
    }

    this.form.get('idAsignatura')?.enable();

    this.subs.add(
      this.convocatoriaService.getAsignaturasPorDocente(idDocente).pipe(catchError(() => of([]))).subscribe({
        next: (data) => {
          this.asignaturas = (data || []) as AsignaturaComboDTO[];
        },
        error: () => {
          this.asignaturas = [];
        }
      })
    );
  }

  abrirModalCrear() {
    this.modoEdicion = false;
    this.convocatoriaEditId = null;
    this.form.reset({
      idAsignatura: null,
      idDocente: null,
      idPeriodoAcademico: this.periodoActivo?.idPeriodoAcademico ?? null,
      cuposDisponibles: 1,
      estado: 'ABIERTA',
      activo: true,
    });
    this.asignaturas = [];
    this.form.get('idAsignatura')?.disable();
    this.mostrarModal = true;
  }

  abrirModalEditar(conv: ConvocatoriaDTO) {
    this.modoEdicion = true;
    this.convocatoriaEditId = conv.idConvocatoria ?? null;

    if (conv.idPeriodoAcademico && conv.nombrePeriodo && !this.periodosMap.has(conv.idPeriodoAcademico)) {
      this.periodosMap.set(conv.idPeriodoAcademico, conv.nombrePeriodo);
    }

    this.form.patchValue({
      idAsignatura: conv.idAsignatura ?? null,
      idDocente: conv.idDocente ?? null,
      idPeriodoAcademico: conv.idPeriodoAcademico ?? this.periodoActivo?.idPeriodoAcademico ?? null,
      cuposDisponibles: conv.cuposDisponibles ?? 1,
      fechaPublicacion: this.formatFechaInput(conv.fechaPublicacion),
      fechaCierre: this.formatFechaInput(conv.fechaCierre),
      estado: conv.estado ?? 'ABIERTA',
      activo: conv.activo ?? true,
    });

    const idDocente = conv.idDocente ?? null;
    if (idDocente) {
      this.form.get('idAsignatura')?.enable();
      this.subs.add(
        this.convocatoriaService.getAsignaturasPorDocente(idDocente).pipe(catchError(() => of([]))).subscribe({
          next: (data) => {
            this.asignaturas = (data || []) as AsignaturaComboDTO[];
          },
        })
      );
    } else {
      this.asignaturas = [];
      this.form.get('idAsignatura')?.disable();
    }

    this.mostrarModal = true;
  }

  cerrarModal() { this.mostrarModal = false; }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loadingForm = true;
    const payload = {
      ...this.form.getRawValue(),
      idConvocatoria: this.modoEdicion ? this.convocatoriaEditId : undefined,
      idPeriodoAcademico: this.periodoActivo?.idPeriodoAcademico ?? this.form.getRawValue().idPeriodoAcademico,
    };

    const req = (this.modoEdicion && this.convocatoriaEditId)
      ? this.http.put<ConvocatoriaDTO>(`${API_CONV}/actualizar`, payload)
      : this.http.post<ConvocatoriaDTO>(`${API_CONV}/crear`, payload);

    this.subs.add(req.subscribe({
      next: (resp) => {
        if (this.modoEdicion) {
          const idx = this.convocatorias.findIndex(c => c.idConvocatoria === this.convocatoriaEditId);
          if (idx > -1) this.convocatorias[idx] = resp;
        } else {
          this.convocatorias = [resp, ...this.convocatorias];
        }
        this.showSuccess(this.modoEdicion ? 'Convocatoria actualizada.' : 'Convocatoria creada.');
        this.cerrarModal();
        this.loadingForm = false;
      },
      error: (err) => {
        this.errorMensaje = err?.error?.message || 'Error al guardar.';
        this.loadingForm = false;
        setTimeout(() => this.errorMensaje = '', 4000);
      }
    }));
  }

  eliminar(conv: ConvocatoriaDTO) {
    if (!confirm(`¿Eliminar "${conv.nombreAsignatura}"?`)) return;
    this.subs.add(
      this.http.delete(`${API_CONV}/${conv.idConvocatoria}`).subscribe({
        next: () => {
          this.convocatorias = this.convocatorias.filter(c => c.idConvocatoria !== conv.idConvocatoria);
          this.showSuccess('Convocatoria eliminada.');
        },
        error: () => { this.errorMensaje = 'No se pudo eliminar.'; setTimeout(() => this.errorMensaje = '', 3000); }
      })
    );
  }

  formatFecha(fecha: any): any {
    if (!fecha) return null;
    if (Array.isArray(fecha)) {
      return new Date(fecha[0], (fecha[1] ?? 1) - 1, fecha[2] ?? 1);
    }
    return fecha;
  }

  formatFechaInput(fecha: any): string {
    if (!fecha) return '';
    if (Array.isArray(fecha)) {
      const y = fecha[0];
      const m = String(fecha[1]).padStart(2, '0');
      const d = String(fecha[2]).padStart(2, '0');
      return `${y}-${m}-${d}`;
    }
    if (typeof fecha === 'string') return fecha.split('T')[0];
    return '';
  }

  private showSuccess(msg: string) {
    this.successMensaje = msg;
    setTimeout(() => this.successMensaje = '', 3500);
  }
}
