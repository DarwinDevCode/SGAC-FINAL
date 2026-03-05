import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Loader2, FolderOpen, FileText, ClipboardCheck, Award, Users, Flag, Paperclip, Eye, Upload } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO, RequisitoAdjuntoResponseDTO } from '../../../core/dto/postulacion';

@Component({
    selector: 'app-estado-postulacion',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './estado-postulacion.html',
    styleUrl: './estado-postulacion.css',
    providers: [
        {
            provide: LUCIDE_ICONS,
            multi: true,
            useValue: new LucideIconProvider({ Loader2, FolderOpen, FileText, ClipboardCheck, Award, Users, Flag, Paperclip, Eye, Upload })
        }
    ]
})
export class EstadoPostulacionComponent implements OnInit, OnDestroy {
    postulanteService = inject(PostulanteService);
    authService = inject(AuthService);
    http = inject(HttpClient);
    private subs = new Subscription();

    postulaciones: PostulacionResponseDTO[] = [];
    loading = true;
    idEstudianteBase = 0;

    /** Mapa idPostulacion → lista de documentos */
    documentosPorPostulacion: Record<number, RequisitoAdjuntoResponseDTO[]> = {};
    /** Mapa idPostulacion → si está expandido el panel de documentos */
    expandidoPostulacion: Record<number, boolean> = {};
    /** Mapa idRequisito → mensaje de éxito/error al reemplazar */
    mensajeReemplazo: Record<number, string> = {};
    /** Mapa idRequisito → si está subiendo */
    subiendoRequisito: Record<number, boolean> = {};

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (user) {
            this.idEstudianteBase = user.idUsuario;
        }
        this.cargarMisPostulaciones();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarMisPostulaciones() {
        this.loading = true;
        this.subs.add(
            this.postulanteService.misPostulaciones(this.idEstudianteBase).subscribe({
                next: (data) => {
                    this.postulaciones = data || [];
                    this.loading = false;
                },
                error: (err) => {
                    console.error(err.error?.mensaje || err.message || 'Error al cargar postulaciones');
                    this.loading = false;
                }
            })
        );
    }

    toggleDocumentos(idPostulacion: number) {
        const yaExpandido = this.expandidoPostulacion[idPostulacion];
        this.expandidoPostulacion[idPostulacion] = !yaExpandido;
        // Carga documentos la primera vez que se expande
        if (!yaExpandido && !this.documentosPorPostulacion[idPostulacion]) {
            this.subs.add(
                this.postulanteService.listarDocumentosPostulacion(idPostulacion).subscribe({
                    next: (docs) => {
                        this.documentosPorPostulacion[idPostulacion] = docs || [];
                    },
                    error: () => {
                        this.documentosPorPostulacion[idPostulacion] = [];
                    }
                })
            );
        }
    }

    verDocumento(idRequisito: number) {
        this.subs.add(
            this.http.get(`http://localhost:8080/api/requisitos-adjuntos/descargar/${idRequisito}`, {
                responseType: 'blob'
            }).subscribe({
                next: (blob) => {
                    const url = window.URL.createObjectURL(blob);
                    window.open(url, '_blank');
                },
                error: (err) => {
                    console.error('Error al descargar documento:', err);
                    alert('No tiene los permisos suficientes o el documento no existe.');
                }
            })
        );
    }

    onArchivoReemplazo(event: Event, idRequisito: number) {
        const input = event.target as HTMLInputElement;
        if (!input.files || !input.files[0]) return;
        const archivo = input.files[0];
        this.subiendoRequisito[idRequisito] = true;
        this.mensajeReemplazo[idRequisito] = '';
        this.subs.add(
            this.postulanteService.reemplazarDocumento(idRequisito, archivo).subscribe({
                next: (dto) => {
                    this.mensajeReemplazo[idRequisito] = '✔ Documento reemplazado correctamente.';
                    this.subiendoRequisito[idRequisito] = false;
                    // Actualizar el documento en el mapa
                    const idPost = dto.idPostulacion;
                    if (this.documentosPorPostulacion[idPost]) {
                        const idx = this.documentosPorPostulacion[idPost].findIndex(d => d.idRequisitoAdjunto === idRequisito);
                        if (idx !== -1) this.documentosPorPostulacion[idPost][idx] = dto;
                    }
                },
                error: (e) => {
                    this.mensajeReemplazo[idRequisito] = '✖ Error: ' + (e.error?.message || e.message || 'No se pudo reemplazar el documento.');
                    this.subiendoRequisito[idRequisito] = false;
                }
            })
        );
    }

    getPhase(estado: string | undefined): number {
        if (!estado) return 1;
        const e = estado.toUpperCase();
        if (e === 'PENDIENTE') return 1;
        if (e === 'EN_EVALUACION' || e === 'MERITOS_EVALUADOS') return 2;
        if (e === 'OPOSICION_EVALUADA') return 3;
        if (e === 'APROBADO' || e === 'RECHAZADO') return 4;
        return 1;
    }

    getEstadoClass(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'APROBADO': return 'doc-estado aprobado';
            case 'RECHAZADO': return 'doc-estado rechazado';
            case 'OBSERVADO': return 'doc-estado observado';
            default: return 'doc-estado pendiente';
        }
    }
}
