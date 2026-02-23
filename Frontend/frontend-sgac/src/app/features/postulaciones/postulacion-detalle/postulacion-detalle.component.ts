import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideAngularModule, CheckCircle, Clock, FileText, AlertCircle, XCircle } from 'lucide-angular';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import {PostulacionService} from '../../../core/services/postulacion-service';
import {EvaluacionMeritosService} from '../../../core/services/evaluacion-meritos-service';
import {EvaluacionOposicionService} from '../../../core/services/evaluacion-oposicion-service';
import {PostulacionDTO} from '../../../core/dto/postulacion';
import {EvaluacionMeritosDTO} from '../../../core/dto/evaluacion-meritos';
import {EvaluacionOposicionDTO} from '../../../core/dto/evaluacion-oposicion';

@Component({
    selector: 'app-postulacion-detalle',
    standalone: true,
    imports: [CommonModule, LucideAngularModule, StatusChipComponent],
    templateUrl: './postulacion-detalle.component.html',
    styles: []
})
export class PostulacionDetalleComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private postulacionService = inject(PostulacionService);
    private evaluacionOposicionService = inject(EvaluacionOposicionService);
    private evaluacionMeritoService = inject(EvaluacionMeritosService);


    postulacion = signal<PostulacionDTO | null>(null);
    meritos = signal<EvaluacionMeritosDTO | null>(null);
    oposicion = signal<EvaluacionOposicionDTO | null>(null);
    loading = signal(true);

    // Icons
    readonly CheckCircle = CheckCircle;
    readonly Clock = Clock;
    readonly FileText = FileText;
    readonly AlertCircle = AlertCircle;
    readonly XCircle = XCircle;

    timelineSteps = [
        { title: 'Registro de solicitud', description: 'Postulación enviada' },
        { title: 'Documentos en revisión', description: 'Validación por secretaría' },
        { title: 'Validación de requisitos', description: 'Revisión académica' },
        { title: 'Postulación completa', description: 'Resultado final' }
    ];

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadData(+id);
        }
    }

    loadData(id: number) {
        this.loading.set(true);
        const idEstudiante = 1; // HARDCODED
        this.postulacionService.listarPorEstudiante(idEstudiante).subscribe({
            next: (list: PostulacionDTO[]) => {
                const found = list.find((p: PostulacionDTO) => p.idPostulacion === id);
                if (found) {
                    this.postulacion.set(found);
                    this.loadEvaluations(found.idPostulacion);
                } else {
                    this.loading.set(false);
                    // Handle not found
                }
            },
            error: (err: any) => {
                console.error(err);
                this.loading.set(false);
            }
        });
    }

    loadEvaluations(idPostulacion: number) {
        this.evaluacionMeritoService.obtenerMeritosPorPostulacion(idPostulacion).subscribe({
            next: (data: EvaluacionMeritosDTO) => this.meritos.set(data),
            error: () => console.log('Sin méritos')
        });

        this.evaluacionOposicionService.obtenerOposicionPorPostulacion(idPostulacion).subscribe({
            next: (data: EvaluacionOposicionDTO) => this.oposicion.set(data),
            error: () => console.log('Sin oposición'),
            complete: () => this.loading.set(false)
        });
    }

    getCurrentStepIndex(): number {
        const estado = this.postulacion()?.estadoPostulacion;
        switch (estado) {
            case 'PENDIENTE': return 1;
            case 'EN_REVISION': return 2;
            case 'APROBADO':
            case 'RECHAZADO': return 4;
            default: return 0;
        }
    }

    getStepStatus(stepIndex: number): 'completed' | 'current' | 'pending' | 'error' {
        const currentStep = this.getCurrentStepIndex();
        const estadoFinal = this.postulacion()?.estadoPostulacion;

        if (stepIndex < currentStep) return 'completed';
        if (stepIndex === currentStep) {
            if (stepIndex === 3 && estadoFinal === 'RECHAZADO') return 'error';
            return 'current';
        }
        return 'pending';
    }

    get meritosTotal(): number {
        const m = this.meritos();
        if (!m) return 0;
        return (m.notaAsignatura || 0) + (m.notaSemestres || 0) + (m.notaEventos || 0) + (m.notaExperiencia || 0);
    }

    get oposicionTotal(): number {

        return 0;
    }
    get isObservada(): boolean {
        return this.postulacion()?.estadoPostulacion === 'OBSERVADA';
    }

    onCorregir(): void {
        const p = this.postulacion();
        if (p) {

            this.router.navigate(['/student/postulaciones/postular', p.idConvocatoria]);
        }
    }
}
