import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StepperComponent } from '../../../shared/components/stepper/stepper.component';
import { LucideAngularModule, Upload, FileText, CheckCircle, AlertCircle, ArrowRight, ArrowLeft } from 'lucide-angular';
import {PostulacionService} from '../../../core/services/postulacion-service';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';
import {AuthService} from '../../../core/services/auth-service';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';
import {PostulacionDTO} from '../../../core/dto/postulacion';


@Component({
    selector: 'app-postular',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, StepperComponent, LucideAngularModule],
    templateUrl: './postular.component.html'
})
export class PostularComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    private postulacionService = inject(PostulacionService);
    private convocatoriasService = inject(ConvocatoriaService); // To show convoc details
    private authService = inject(AuthService);


    // Constants
    readonly STEPS = ['Requisitos', 'Documentos', 'Confirmación'];

    // State
    currentStep = signal(0);
    idConvocatoria = 0;
    convocatoria = signal<ConvocatoriaDTO | null>(null);
    requisitos = signal<any[]>([]);
    loading = signal(false);

    selectedFiles = new Map<number, File>();

    form: FormGroup = this.fb.group({
        observaciones: ['']
    });

    readonly Upload = Upload;
    readonly FileText = FileText;
    readonly CheckCircle = CheckCircle;
    readonly AlertCircle = AlertCircle;
    readonly ArrowRight = ArrowRight;
    readonly ArrowLeft = ArrowLeft;

    ngOnInit() {
        this.route.paramMap.subscribe(params => {
            this.idConvocatoria = Number(params.get('id'));
            if (this.idConvocatoria) {
                this.loadData();
            }
        });
    }

    loadData() {
        this.loading.set(true);
        // Load Convocatoria Details
        this.convocatoriasService.getById(this.idConvocatoria).subscribe({
            next: (data) => this.convocatoria.set(data),
            error: (err) => console.error('Error loading convocatoria', err)
        });

        // Load Requirement Types
        this.postulacionService.getRequisitosActivos().subscribe({
            next: (data) => {
                this.requisitos.set(data);
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Error loading requirements', err);
                this.loading.set(false);
            }
        });
    }

    onFileSelected(event: any, reqId: number) {
        const file = event.target.files[0];
        if (file) {
            this.selectedFiles.set(reqId, file);
        }
    }

    getFile(reqId: number): File | undefined {
        return this.selectedFiles.get(reqId);
    }

    get canProceedToConfirm(): boolean {
        // Check if a file is selected for every requirement
        return this.requisitos().every(req => this.selectedFiles.has(req.idTipoRequisito));
    }

    nextStep() {
        if (this.currentStep() === 1 && !this.canProceedToConfirm) {
            alert('Por favor sube todos los documentos requeridos.');
            return;
        }
        this.currentStep.update(v => Math.min(v + 1, this.STEPS.length - 1));
    }

    prevStep() {
        this.currentStep.update(v => Math.max(v - 1, 0));
    }

    onSubmit() {
        if (!this.form.valid) return;

        this.loading.set(true);
        const currentUser = this.authService.getCurrentUser();


        const requestData = {
          idConvocatoria: this.idConvocatoria,
          idEstudiante: currentUser?.idUsuario!,
          observaciones: this.form.get('observaciones')?.value || ''
        } as PostulacionDTO;


        const files: File[] = [];
        const tiposRequisito: number[] = [];

        this.requisitos().forEach(req => {
            const file = this.selectedFiles.get(req.idTipoRequisito);
            if (file) {
                files.push(file);
                tiposRequisito.push(req.idTipoRequisito);
            }
        });

        // 3. Call Service
        this.postulacionService.registrar(requestData, files, tiposRequisito).subscribe({
            next: () => {
                this.loading.set(false);
                this.router.navigate(['/student/postulaciones/mis-postulaciones']);
            },
            error: (err) => {
                console.error('Error submitting', err);
                this.loading.set(false);
                // Show more detailed error if available
                const msg = err.error?.message || 'Ocurrió un error al registrar la postulación.';
                alert(msg);
            }
        });
    }
}
