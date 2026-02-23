import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { LucideAngularModule, Save, ArrowLeft, Calendar } from 'lucide-angular';
import {ConvocatoriaService} from '../../../../core/services/convocatoria-service';
import {PeriodoAcademicoDTO} from '../../../../core/dto/periodo-academico';
import {AsignaturaDTO} from '../../../../core/dto/asignatura';

@Component({
    selector: 'app-convocatoria-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, LucideAngularModule],
    templateUrl: './convocatoria-form.component.html'
})
export class ConvocatoriaFormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private router = inject(Router);
    private route = inject(ActivatedRoute);
    private convocatoriasService = inject(ConvocatoriaService);

    form: FormGroup;
    isEdit = false;
    idConvocatoria = 0;
    loading = false;

    // Catalogs
    periodos = signal<PeriodoAcademicoDTO[]>([]);
    asignaturas = signal<AsignaturaDTO[]>([]);

    // Icons
    readonly Save = Save;
    readonly ArrowLeft = ArrowLeft;
    readonly Calendar = Calendar;

    constructor() {
        this.form = this.fb.group({
            idPeriodoAcademico: ['', Validators.required],
            idAsignatura: ['', Validators.required],
            cuposDisponibles: [1, [Validators.required, Validators.min(1)]],
            fechaPublicacion: ['', Validators.required],
            fechaCierre: ['', Validators.required],
            idDocente: [1] // Default
        });
    }

    ngOnInit() {
        this.loadCatalogs();

        // Check if edit mode
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.isEdit = true;
            this.idConvocatoria = +id;
            this.loadData(this.idConvocatoria);
        }
    }

  loadCatalogs() {
    // Mocked or real if endpoints exist
    this.convocatoriasService.getPeriodoActivo().subscribe({
      next: (p) => {
        const arrayPeriodos = Array.isArray(p) ? p : [p];
        this.periodos.set(arrayPeriodos);
      },
      error: () => this.periodos.set([{
        idPeriodoAcademico: 1,
        nombrePeriodo: '2024-1',
        fechaInicio: '2024-01-01',
        fechaFin: '2024-06-01',
        estado: 'ACTIVO',
        activo: true
      }])
    });

    this.convocatoriasService.getAsignaturas().subscribe({
      next: (data) => this.asignaturas.set(data),
      error: () => {
        // Mock fallback
        this.asignaturas.set([
          { idAsignatura: 1, nombreAsignatura: 'Ingeniería de Software', nombreCarrera: 'Software', semestre: 5 },
          { idAsignatura: 2, nombreAsignatura: 'Física I', nombreCarrera: 'Software', semestre: 1 }
        ]);
      }
    });
  }

    loadData(id: number) {
        this.convocatoriasService.getById(id).subscribe(data => {
            this.form.patchValue({
                idPeriodoAcademico: data.idPeriodoAcademico,
                idAsignatura: data.idAsignatura,
                cuposDisponibles: data.cuposDisponibles,
                fechaPublicacion: data.fechaPublicacion,
                fechaCierre: data.fechaCierre
            });
        });
    }

    onSubmit() {
        if (this.form.invalid) return;

        this.loading = true;
        const val = this.form.value;
        const req: any = {
            ...val,
            idConvocatoria: this.isEdit ? this.idConvocatoria : undefined,
            estado: 'BORRADOR', // Default status
            activo: true
        };

        const obs$ = this.isEdit
            ? this.convocatoriasService.update(req)
            : this.convocatoriasService.create(req);

        obs$.subscribe({
            next: () => {
                alert('Convocatoria guardada correctamente');
                this.router.navigate(['/dean/convocatorias']);
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
                alert('Error al guardar');
            }
        });
    }
}
