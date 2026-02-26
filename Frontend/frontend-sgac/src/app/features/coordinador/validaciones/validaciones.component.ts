import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription, switchMap } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';

@Component({
    selector: 'app-coordinador-validaciones',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, LucideAngularModule],
    templateUrl: './validaciones.html',
    styleUrl: './validaciones.css',
})
export class ValidacionesComponent implements OnInit, OnDestroy {
    private coordinadorService = inject(CoordinadorService);
    private authService = inject(AuthService);
    private subs = new Subscription();

    postulantes: PostulacionResponseDTO[] = [];
    loading = true;
    errorMensaje = '';
    successMensaje = '';

    // Modal
    mostrarModal = false;
    postulacionSeleccionada: PostulacionResponseDTO | null = null;
    nuevoEstado = 'EN_EVALUACION';
    observacion = '';
    loadingAccion = false;

    // Documentos del postulante
    documentos: any[] = [];
    loadingDocs = false;

    readonly estadosValidos = ['EN_EVALUACION', 'RECHAZADO'];

    ngOnInit(): void {
        this.cargarPostulantes();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarPostulantes() {
        this.loading = true;
        this.errorMensaje = '';
        const user = this.authService.getUser();
        if (!user) { this.loading = false; return; }

        this.subs.add(
            this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).pipe(
                switchMap((coord: any) => {
                    return this.coordinadorService.listarPendientesPorCarrera(coord.idCarrera ?? 0);
                })
            ).subscribe({
                next: (lista) => {
                    this.postulantes = lista;
                    this.loading = false;
                },
                error: () => {
                    this.errorMensaje = 'Error al cargar postulaciones.';
                    this.loading = false;
                }
            })
        );
    }

    abrirModal(p: PostulacionResponseDTO) {
        this.postulacionSeleccionada = p;
        this.nuevoEstado = 'EN_EVALUACION';
        this.observacion = '';
        this.documentos = [];
        this.mostrarModal = true;

        // Cargar los documentos adjuntos
        if (p.idPostulacion) {
            this.loadingDocs = true;
            this.subs.add(
                this.coordinadorService.listarDocumentosPorPostulacion(p.idPostulacion).subscribe({
                    next: (docs) => {
                        this.documentos = docs;
                        this.loadingDocs = false;
                    },
                    error: () => {
                        this.loadingDocs = false;
                    }
                })
            );
        }
    }

    cerrarModal() {
        this.mostrarModal = false;
        this.postulacionSeleccionada = null;
        this.documentos = [];
    }

    getUrlDescarga(idRequisito: number): string {
        return this.coordinadorService.getUrlDescargaDocumento(idRequisito);
    }

    confirmarDecision() {
        if (!this.postulacionSeleccionada?.idPostulacion) return;
        this.loadingAccion = true;

        this.subs.add(
            this.coordinadorService.cambiarEstadoPostulacion(
                this.postulacionSeleccionada.idPostulacion,
                this.nuevoEstado,
                this.observacion || '—'
            ).subscribe({
                next: () => {
                    const estadoMsg = this.nuevoEstado === 'EN_EVALUACION' ? 'aprobada para evaluación' : 'rechazada';
                    this.successMensaje = `Postulación ${estadoMsg} correctamente.`;
                    this.loadingAccion = false;
                    this.cerrarModal();
                    this.cargarPostulantes();
                    setTimeout(() => this.successMensaje = '', 4000);
                },
                error: () => {
                    this.errorMensaje = 'Error al actualizar el estado.';
                    this.loadingAccion = false;
                    setTimeout(() => this.errorMensaje = '', 4000);
                }
            })
        );
    }
}
