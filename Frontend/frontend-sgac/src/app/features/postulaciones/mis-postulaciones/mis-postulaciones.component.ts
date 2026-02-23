import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import {PostulacionDTO} from '../../../core/dto/postulacion';
import {PostulacionService} from '../../../core/services/postulacion-service';
// import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-mis-postulaciones',
    standalone: true,
    imports: [CommonModule, RouterModule, StatusChipComponent],
    templateUrl: './mis-postulaciones.component.html',
    styleUrl: './mis-postulaciones.component.css'
})
export class MisPostulacionesComponent implements OnInit {
    postulaciones: PostulacionDTO[] = [];
    postulacionService = inject(PostulacionService);
    // authService = inject(AuthService);

    timelineSteps = [
        { title: 'Registro de solicitud', description: 'Postulación enviada' },
        { title: 'Documentos en revisión', description: 'Validación por secretaría' },
        { title: 'Validación de requisitos', description: 'Revisión académica' },
        { title: 'Postulación completa', description: 'Resultado final' }
    ];

    ngOnInit(): void {
        // const idEstudiante = this.authService.getCurrentUser()?.id; // Need to map user to student
        const idEstudiante = 1; // HARDCODED for now as discussed
        this.postulacionService.listarPorEstudiante(idEstudiante).subscribe({
            next: (data) => this.postulaciones = data,
            error: (err) => console.error(err)
        });
    }

    getCurrentStepIndex(estado: string): number {
        switch (estado) {
            case 'PENDIENTE': return 1; // Revision
            case 'EN_REVISION': return 2; // Validacion
            case 'APROBADO':
            case 'RECHAZADO': return 4; // Completa
            default: return 0;
        }
    }

    getStepStatus(stepIndex: number, currentStep: number, estadoFinal: string): 'completed' | 'current' | 'pending' | 'error' {
        if (stepIndex < currentStep) return 'completed';
        if (stepIndex === currentStep) {
            if (stepIndex === 3 && estadoFinal === 'RECHAZADO') return 'error'; // Final step rejected
            return 'current';
        }
        return 'pending';
    }
}
