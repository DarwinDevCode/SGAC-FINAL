import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DocenteService } from '../../../core/services/docente-service';
import { EvaluacionDesempenoService } from '../../../core/services/evaluacion-desempeno-service';
import { AuthService } from '../../../core/services/auth-service';
import { RegistroActividadDocenteDTO, CambiarEstadoRequest } from '../../../core/dto/docente';
import { LucideAngularModule } from 'lucide-angular';
import { ChatInternoComponent } from '../comunicacion/chat-interno/chat-interno.component';

@Component({
    selector: 'app-actividades-ayudante',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule, ChatInternoComponent],
    templateUrl: './actividades-ayudante.html',
    styleUrl: './actividades-ayudante.css'
})
export class ActividadesAyudanteComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private docenteService = inject(DocenteService);
    private evaluacionService = inject(EvaluacionDesempenoService);
    private authService = inject(AuthService);

    idAyudantia!: number;
    actividades: RegistroActividadDocenteDTO[] = [];
    loading = true;

    // Modal observacion
    modalVisible = false;
    modalTipo: 'actividad' | 'evidencia' | 'evaluacion' = 'actividad';
    modalEstado = '';
    modalObservacion = '';
    modalIdReferencia = 0;
    guardando = false;

    // Evaluación de desempeño
    puntajeEvaluacion = 10;
    retroalimentacionEvaluacion = '';

    readonly ESTADOS = ['PENDIENTE', 'ACEPTADO', 'RECHAZADO', 'OBSERVADO'];

    ngOnInit(): void {
        this.idAyudantia = Number(this.route.snapshot.paramMap.get('idAyudantia'));
        this.cargarActividades();
    }

    cargarActividades(): void {
        this.loading = true;
        this.docenteService.getActividadesAyudante(this.idAyudantia).subscribe({
            next: (a) => { this.actividades = a; this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    toggleActividad(act: RegistroActividadDocenteDTO): void {
        act.expandido = !act.expandido;
    }

    iniciarCambioActividad(idActividad: number, estadoActual: string): void {
        this.modalTipo = 'actividad';
        this.modalIdReferencia = idActividad;
        this.modalEstado = estadoActual;
        this.modalObservacion = '';
        this.modalVisible = true;
    }

    iniciarCambioEvidencia(idEvidencia: number, estadoActual: string): void {
        this.modalTipo = 'evidencia';
        this.modalIdReferencia = idEvidencia;
        this.modalEstado = estadoActual;
        this.modalObservacion = '';
        this.modalVisible = true;
    }

    iniciarEvaluacion(idActividad: number): void {
        this.modalTipo = 'evaluacion';
        this.modalIdReferencia = idActividad;
        this.puntajeEvaluacion = 10;
        this.retroalimentacionEvaluacion = '';
        this.modalVisible = true;
    }

    confirmarCambio(): void {
        if (this.modalTipo === 'evaluacion') {
            this.guardarEvaluacion();
            return;
        }

        if (this.modalEstado === 'OBSERVADO' && !this.modalObservacion.trim()) {
            alert('Debes escribir una observación');
            return;
        }
        const req: CambiarEstadoRequest = {
            estado: this.modalEstado,
            observaciones: this.modalObservacion || undefined
        };
        this.guardando = true;
        const obs$ = this.modalTipo === 'actividad'
            ? this.docenteService.cambiarEstadoActividad(this.modalIdReferencia, req)
            : this.docenteService.cambiarEstadoEvidencia(this.modalIdReferencia, req);

        obs$.subscribe({
            next: () => {
                this.modalVisible = false;
                this.guardando = false;
                this.cargarActividades();
            },
            error: () => { this.guardando = false; alert('Error al guardar el estado'); }
        });
    }

    guardarEvaluacion(): void {
        this.guardando = true;
        // Usamos el idUsuario del docente autenticado
        const idDocente = this.authService.getUser()?.idUsuario ?? 0;
        this.evaluacionService.evaluarSesion(this.modalIdReferencia, idDocente, this.puntajeEvaluacion, this.retroalimentacionEvaluacion).subscribe({
            next: () => {
                this.modalVisible = false;
                this.guardando = false;
                alert('Evaluación guardada correctamente');
            },
            error: () => { this.guardando = false; alert('Error al guardar la evaluación'); }
        });
    }

    cerrarModal(): void { this.modalVisible = false; }

    volver(): void { this.router.navigate(['/docente/mis-ayudantes']); }

    getEstadoClass(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'ACEPTADO': return 'estado-aceptado';
            case 'RECHAZADO': return 'estado-rechazado';
            case 'OBSERVADO': return 'estado-observado';
            default: return 'estado-pendiente';
        }
    }

    getEstadoIcon(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'ACEPTADO': return 'check-circle';
            case 'RECHAZADO': return 'x-circle';
            case 'OBSERVADO': return 'eye';
            default: return 'clock';
        }
    }
}
