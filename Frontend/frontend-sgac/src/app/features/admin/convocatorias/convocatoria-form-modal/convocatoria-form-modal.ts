import { Component, EventEmitter, inject, Input, Output, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConvocatoriaService } from '../../../../core/services/convocatoria-service';
import { ConvocatoriaDTO } from '../../../../core/dto/convocatoria';
import { DocenteDTO } from '../../../../core/dto/docente';
import { AsignaturaDTO } from '../../../../core/dto/asignatura';
import { PeriodoAcademicoDTO } from '../../../../core/dto/periodo-academico';
import { forkJoin } from 'rxjs';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, X, Save, Calendar, Users, BookOpen, GraduationCap } from 'lucide-angular';
import {HttpErrorResponse} from '@angular/common/http';
import {ConvocatoriaCardComponent} from '../../../convocatorias/convocatoria-card/convocatoria-card';

@Component({
  selector: 'app-convocatoria-form-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  providers: [
    {
      provide: LUCIDE_ICONS,
      multi: true,
      useValue: new LucideIconProvider({ X, Save, Calendar, Users, BookOpen, GraduationCap })
    }
  ],
  templateUrl: './convocatoria-form-modal.html',
  styleUrl: './convocatoria-form-modal.css',
})
export class ConvocatoriaFormModalComponent implements OnInit {
  private convocatoriasService = inject(ConvocatoriaService);

  @Input() convocatoria?: ConvocatoriaDTO;
  @Output() close = new EventEmitter<void>();

  loading = signal(false);
  loadingData = signal(true);

  form = signal({
    idPeriodo: 0,
    nombrePeriodo: '',
    idDocente: 0,
    idAsignatura: 0,
    vacantes: 1,
    fechaInicio: '',
    fechaFin: '',
    estado: 'ABIERTA'
  });

  docentesList = signal<DocenteDTO[]>([]);
  asignaturasList = signal<AsignaturaDTO[]>([]);
  periodoActivo = signal<PeriodoAcademicoDTO | null>(null);

  ngOnInit(): void {
    this.loadResources();
  }

  loadResources(): void {
    this.loadingData.set(true);

    forkJoin({
      periodo: this.convocatoriasService.getPeriodoActivo(),
      docentes: this.convocatoriasService.getDocentes(),
      asignaturas: this.convocatoriasService.getAsignaturas()
    }).subscribe({
      next: ({ periodo, docentes, asignaturas }) => {
        this.docentesList.set(docentes || []);
        this.asignaturasList.set(asignaturas || []);

        const periodoReal = Array.isArray(periodo) ? periodo[0] : periodo;
        this.periodoActivo.set(periodoReal || null);

        const p = this.periodoActivo();

        this.form.update(f => ({
          ...f,
          idPeriodo: p?.idPeriodoAcademico || 0,
          nombrePeriodo: p?.nombrePeriodo || ''
        }));

        if (this.convocatoria) {
          this.form.update(f => ({
            ...f,
            idDocente: this.convocatoria!.idDocente,
            idAsignatura: this.convocatoria!.idAsignatura,
            vacantes: this.convocatoria!.cuposDisponibles,
            fechaInicio: this.convocatoria!.fechaPublicacion,
            fechaFin: this.convocatoria!.fechaCierre,
            estado: this.convocatoria!.estado || 'ABIERTA'
          }));
        }

        this.loadingData.set(false);
      },
      error: (error) => {
        console.error('Error loading resources:', error);
        alert('Error al cargar los recursos necesarios');
        this.loadingData.set(false);
        this.onClose();
      }
    });
  }

  onSubmit(): void {
    const formData = this.form();

    if (!formData.idDocente || !formData.idAsignatura || !formData.fechaInicio || !formData.fechaFin) {
      alert('Por favor complete todos los campos obligatorios');
      return;
    }

    if (new Date(formData.fechaInicio) > new Date(formData.fechaFin)) {
      alert('La fecha de inicio no puede ser posterior a la fecha de cierre');
      return;
    }

    this.loading.set(true);

    const request: ConvocatoriaDTO = {
      idConvocatoria: this.convocatoria?.idConvocatoria,
      idPeriodoAcademico: formData.idPeriodo,
      idAsignatura: formData.idAsignatura,
      idDocente: formData.idDocente,
      cuposDisponibles: formData.vacantes,
      fechaPublicacion: formData.fechaInicio,
      fechaCierre: formData.fechaFin,
      estado: formData.estado,
      activo: true
    };

    const operation = this.convocatoria
      ? this.convocatoriasService.update(request)
      : this.convocatoriasService.create(request);

    operation.subscribe({
      next: () => {
        alert(this.convocatoria ? 'Convocatoria actualizada correctamente' : 'Convocatoria creada correctamente');
        this.loading.set(false);
        this.onClose();
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error al guardar la  convocatoria:', error);
        alert(error.error?.message || 'Error al guardar la convocatoria');
        this.loading.set(false);
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }

  protected readonly X = X;
  protected readonly Calendar = Calendar;
  protected readonly GraduationCap = GraduationCap;
  protected readonly BookOpen = BookOpen;
  protected readonly Users = Users;
  protected readonly Save = Save;
}
