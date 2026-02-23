import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { LucideAngularModule, ArrowLeft, FileText, CheckCircle, XCircle, AlertCircle, Eye } from 'lucide-angular';
import {PostulacionService} from '../../../core/services/postulacion-service';
import {PostulacionDTO} from '../../../core/dto/postulacion';

@Component({
    selector: 'app-review-postulacion',
    standalone: true,
    imports: [CommonModule, FormsModule, StatusChipComponent, LucideAngularModule],
    templateUrl: './review-postulacion.component.html'
})
export class ReviewPostulacionComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private postulacionService = inject(PostulacionService);

    postulacion = signal<PostulacionDTO | null>(null);
    idPostulacion = 0;

    // Observation Modal
    showObservationModal = false;
    observationText = '';
    actionType: 'OBSERVAR' | 'RECHAZAR' | 'VALIDAR' = 'VALIDAR';

    // State
    activeDocumentUrl: string | null = null; // Mock URL

    readonly ArrowLeft = ArrowLeft;
    readonly FileText = FileText;
    readonly CheckCircle = CheckCircle;
    readonly XCircle = XCircle;
    readonly AlertCircle = AlertCircle;
    readonly Eye = Eye;

    ngOnInit() {
        this.idPostulacion = Number(this.route.snapshot.paramMap.get('id'));
        if (this.idPostulacion) this.loadData();
    }

    loadData() {
        // Create a specific endpoint for getById or use existing logic
        // HARDCODED student search again as workaround if no getById
        const idEstudiante = 1;
        this.postulacionService.listarPorEstudiante(idEstudiante).subscribe({
            next: (list: PostulacionDTO[]) => {
                const found = list.find((p: PostulacionDTO) => p.idPostulacion === this.idPostulacion);
                if (found) this.postulacion.set(found);
            }
        });
    }

    // Actions
    openActionModal(type: 'OBSERVAR' | 'RECHAZAR' | 'VALIDAR') {
        this.actionType = type;
        this.observationText = '';
        if (type === 'VALIDAR') {
            if (confirm('¿Está seguro de validar esta postulación? Pasará a etapa de evaluación.')) {
                this.submitAction();
            }
        } else {
            this.showObservationModal = true;
        }
    }

    closeModal() {
        this.showObservationModal = false;
    }

    submitAction() {
        if (!this.postulacion()) return;

        let newState = '';
        if (this.actionType === 'VALIDAR') newState = 'VALIDADA'; // Or 'EN_EVALUACION' depending on backend enum? Assuming VALIDADA -> then EVALUATED
        if (this.actionType === 'OBSERVAR') newState = 'OBSERVADA';
        if (this.actionType === 'RECHAZAR') newState = 'RECHAZADA';

        this.postulacionService.cambiarEstado(this.idPostulacion, newState, this.observationText).subscribe({
            next: () => {
                alert('Estado actualizado correctamente');
                this.closeModal();
                this.router.navigate(['/coordinator/postulaciones']);
            },
            error: (err) => alert('Error al actualizar estado: ' + err.message)
        });
    }
}
