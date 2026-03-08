import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Loader2, FolderOpen, FileText, ClipboardCheck, Award, Users, Flag, Paperclip, Eye, Upload, AlertTriangle, Calendar, Clock, CheckCircle, XCircle, AlertCircle } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import {
    DetallePostulacionResponseDTO,
    EtapaCronogramaDTO,
    DocumentoPostulacionDTO,
    PostulacionInfoDTO,
    ConvocatoriaPostulacionDTO,
    ResumenDocumentosDTO,
    SubsanacionDocumentoResponseDTO
} from '../../../core/dto/detalle-postulacion';

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
            useValue: new LucideIconProvider({
                Loader2, FolderOpen, FileText, ClipboardCheck, Award, Users, Flag,
                Paperclip, Eye, Upload, AlertTriangle, Calendar, Clock, CheckCircle,
                XCircle, AlertCircle
            })
        }
    ]
})
export class EstadoPostulacionComponent implements OnInit, OnDestroy {
    postulanteService = inject(PostulanteService);
    authService = inject(AuthService);
    http = inject(HttpClient);
    private subs = new Subscription();

    // Estados de carga
    loading = true;
    idUsuario = 0;

    // Datos de la postulación activa
    tienePostulacion = false;
    postulacion: PostulacionInfoDTO | null = null;
    convocatoria: ConvocatoriaPostulacionDTO | null = null;
    cronograma: EtapaCronogramaDTO[] = [];
    documentos: DocumentoPostulacionDTO[] = [];
    resumenDocumentos: ResumenDocumentosDTO | null = null;

    // Mensajes
    mensajeError: string | null = null;
    codigoError: string | null = null;

    // Control de documentos
    mensajeReemplazo: Record<number, string> = {};
    subiendoRequisito: Record<number, boolean> = {};

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (user) {
            this.idUsuario = user.idUsuario;
        }
        this.cargarMiPostulacionActiva();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarMiPostulacionActiva() {
        this.loading = true;
        this.mensajeError = null;

        this.subs.add(
            this.postulanteService.obtenerMiPostulacionActiva(this.idUsuario).subscribe({
                next: (response: DetallePostulacionResponseDTO) => {
                    if (response.exito) {
                        this.tienePostulacion = true;
                        this.postulacion = response.postulacion || null;
                        this.convocatoria = response.convocatoria || null;
                        this.cronograma = response.cronograma || [];
                        this.documentos = response.documentos || [];
                        this.resumenDocumentos = response.resumen_documentos || null;
                    } else {
                        this.tienePostulacion = false;
                        this.codigoError = response.codigo || null;
                        this.mensajeError = response.mensaje;
                    }
                    this.loading = false;
                },
                error: (err) => {
                    console.error('Error al cargar postulación:', err);
                    this.mensajeError = 'Error al cargar la información de tu postulación.';
                    this.loading = false;
                }
            })
        );
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
            this.postulanteService.subsanarDocumentoObservado(this.idUsuario, idRequisito, archivo).subscribe({
                next: (response: SubsanacionDocumentoResponseDTO) => {
                    this.subiendoRequisito[idRequisito] = false;

                    if (response.exito) {
                        this.mensajeReemplazo[idRequisito] = '✔ ' + response.mensaje;
                        // Recargar datos para ver el nuevo estado
                        this.cargarMiPostulacionActiva();
                    } else {
                        // Mostrar mensaje de error específico (fuera de periodo, estado incorrecto, etc.)
                        this.mensajeReemplazo[idRequisito] = '✖ ' + response.mensaje;
                    }
                },
                error: (e) => {
                    this.subiendoRequisito[idRequisito] = false;
                    const errorMsg = e.error?.mensaje || e.error?.message || e.message || 'No se pudo subsanar el documento.';
                    this.mensajeReemplazo[idRequisito] = '✖ Error: ' + errorMsg;
                }
            })
        );
    }

    // Helpers para el estado de las etapas del cronograma
    getEtapaIcono(nombre: string): string {
        switch (nombre.toUpperCase()) {
            case 'POSTULACIÓN': return 'clipboard-check';
            case 'REVISIÓN': return 'eye';
            case 'RESULTADOS': return 'flag';
            default: return 'clock';
        }
    }

    getEtapaClase(estado: string): string {
        switch (estado) {
            case 'COMPLETADA': return 'etapa-completada';
            case 'EN_CURSO': return 'etapa-activa';
            default: return 'etapa-pendiente';
        }
    }

    // Helpers para el estado de documentos
    getEstadoDocumentoClase(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'APROBADO': return 'doc-estado aprobado';
            case 'RECHAZADO': return 'doc-estado rechazado';
            case 'OBSERVADO': return 'doc-estado observado';
            default: return 'doc-estado pendiente';
        }
    }

    getEstadoDocumentoIcono(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'APROBADO': return 'check-circle';
            case 'RECHAZADO': return 'x-circle';
            case 'OBSERVADO': return 'alert-circle';
            default: return 'clock';
        }
    }

    // Helper para formatear fechas
    formatearFecha(fecha: string): string {
        if (!fecha) return 'N/A';
        try {
            const date = new Date(fecha);
            return date.toLocaleDateString('es-EC', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch {
            return fecha;
        }
    }

    // Helper para estado de postulación
    getEstadoPostulacionClase(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'APROBADO':
            case 'APTO': return 'status-pill aprobado';
            case 'RECHAZADO':
            case 'NO_APTO': return 'status-pill rechazado';
            case 'EN_REVISION':
            case 'EN_EVALUACION': return 'status-pill info';
            case 'OBSERVADO': return 'status-pill observado';
            default: return 'status-pill pendiente';
        }
    }
}
