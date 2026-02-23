import {Component, EventEmitter, inject, Input, Output, signal} from '@angular/core';
import {ConvocatoriaService} from '../../../../core/services/convocatoria-service';
import {ConvocatoriaDTO} from '../../../../core/dto/convocatoria';
import {DocenteDTO} from '../../../../core/dto/docente';
import {AsignaturaDTO} from '../../../../core/dto/asignatura';
import {PeriodoAcademicoDTO} from '../../../../core/dto/periodo-academico';
import {forkJoin} from 'rxjs';
import { LucideAngularModule, X, Save, Calendar, Users, BookOpen, GraduationCap } from 'lucide-angular';

@Component({
  selector: 'app-convocatoria-form-modal',
  imports: [],
  templateUrl: './convocatoria-form-modal.html',
  styleUrl: './convocatoria-form-modal.css',
})
export class ConvocatoriaFormModal {
  private convocatoriasService = inject(ConvocatoriaService);

  @Input() convocatoria?: ConvocatoriaDTO;
  @Output() close = new EventEmitter<void>();

  readonly X = X;
  readonly Save = Save;
  readonly Calendar = Calendar;
  readonly Users = Users;
  readonly BookOpen = BookOpen;
  readonly GraduationCap = GraduationCap;

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

  docentes = signal<DocenteDTO[]>([]);
  asignaturas = signal<AsignaturaDTO[]>([]);
  periodo = signal<PeriodoAcademicoDTO | null>(null);

  docentesList: DocenteDTO[] = [];
  asignaturasList: AsignaturaDTO[] = [];
  periodosList: PeriodoAcademicoDTO | undefined;

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
        this.docentesList = docentes;
        this.asignaturasList = asignaturas;
        this.periodosList = periodo? | undefined;


        this.form.update(f => ({
          ...f,
          idPeriodo: this.periodosList?.idPeriodoAcademico,
          nombrePeriodo: this.periodosList.nombrePeriodo
        }));

        if (this.convocatoria) {
          this.form.update(f => ({
            ...f,
            idDocente: this.convocatoria!.idDocente,
            idAsignatura: this.convocatoria!.idAsignatura,
            vacantes: this.convocatoria!.cuposDisponibles,
            fechaInicio: this.convocatoria!.fechaPublicacion,
            fechaFin: this.convocatoria!.fechaCierre,
            estado: this.convocatoria!.estado
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
      error: (error) => {
        console.error('Error saving convocatoria:', error);
        alert(error.error?.message || 'Error al guardar la convocatoria');
        this.loading.set(false);
      }
    });
  }

  onClose(): void {
    this.close.emit();
  }
}
