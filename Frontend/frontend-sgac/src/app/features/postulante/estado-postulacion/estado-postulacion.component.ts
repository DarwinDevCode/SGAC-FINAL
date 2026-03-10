import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Loader2, FolderOpen, FileText, ClipboardCheck, Award, Users, Flag, Paperclip, Eye, Upload, AlertTriangle, Calendar, Clock, CheckCircle, XCircle, AlertCircle, Timer } from 'lucide-angular';
import { Subscription, interval } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import {
    DetallePostulacionResponseDTO,
    EtapaCronogramaDTO,
    DocumentoPostulacionDTO,
    DocumentoDetalleDTO,
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
                XCircle, AlertCircle, Timer
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
    /** Documentos con información de plazos (del detalle directo) */
    documentosDetalle: DocumentoDetalleDTO[] = [];
    resumenDocumentos: ResumenDocumentosDTO | null = null;

    /**
     * Indica si actualmente es periodo de subsanación.
     * TRUE si estamos en las fases POSTULACION o EVALUACION_REQUISITOS.
     * Controla si se puede reemplazar documentos observados.
     */
    esPeriodoSubsanacion = false;

    /**
     * Indica si la postulación ha sido rechazada definitivamente.
     */
    esPostulacionRechazada = false;

    /**
     * Temporizadores activos para documentos observados (tiempo restante en segundos).
     */
    tiempoRestantePorDocumento: Record<number, number> = {};

    /** Subscription para el intervalo del temporizador */
    private timerSubscription?: Subscription;

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
        this.detenerTemporizadores();
    }

    /**
     * Detiene los temporizadores activos.
     */
    private detenerTemporizadores(): void {
        if (this.timerSubscription) {
            this.timerSubscription.unsubscribe();
        }
    }

    /**
     * Inicia el temporizador para actualizar los contadores de 24h.
     */
    private iniciarTemporizadores(): void {
        this.detenerTemporizadores();

        // Actualizar cada segundo
        this.timerSubscription = interval(1000).subscribe(() => {
            this.actualizarTiemposRestantes();
        });
    }

    /**
     * Actualiza los tiempos restantes de cada documento observado.
     */
    private actualizarTiemposRestantes(): void {
        for (const doc of this.documentosDetalle) {
            if (doc.tiempo_restante_segundos !== undefined && doc.tiempo_restante_segundos !== null) {
                if (doc.tiempo_restante_segundos > 0) {
                    this.tiempoRestantePorDocumento[doc.id_requisito_adjunto] = doc.tiempo_restante_segundos - 1;
                    doc.tiempo_restante_segundos--;
                } else {
                    // Plazo expirado
                    doc.plazo_expirado = true;
                    doc.es_editable = false;
                }
            }
        }
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
                        this.resumenDocumentos = response.resumen_documentos || null;
                        this.esPeriodoSubsanacion = response.es_periodo_subsanacion ?? false;
                        this.esPostulacionRechazada = response.es_postulacion_rechazada ?? false;

                        // Usar documentos del detalle (con info de plazos 24h)
                        this.documentosDetalle = response.documentos || [];

                        // Inicializar tiempos restantes y temporizadores
                        this.inicializarTiemposRestantes();

                        // También cargar documentos del endpoint separado si es necesario
                        if (this.postulacion?.id_postulacion) {
                            this.cargarDocumentosPostulacion(this.postulacion.id_postulacion);
                        }
                    } else {
                        this.tienePostulacion = false;
                        this.codigoError = response.codigo || null;
                        this.mensajeError = response.mensaje;
                        this.esPeriodoSubsanacion = false;
                        this.esPostulacionRechazada = false;
                    }
                    this.loading = false;
                },
                error: (err) => {
                    console.error('Error al cargar postulación:', err);
                    this.mensajeError = 'Error al cargar la información de tu postulación.';
                    this.esPeriodoSubsanacion = false;
                    this.esPostulacionRechazada = false;
                    this.loading = false;
                }
            })
        );
    }

    /**
     * Inicializa los tiempos restantes desde los documentos del detalle e inicia temporizadores.
     */
    private inicializarTiemposRestantes(): void {
        let hayDocumentosConTiempo = false;

        for (const doc of this.documentosDetalle) {
            if (doc.tiempo_restante_segundos !== undefined && doc.tiempo_restante_segundos !== null && doc.tiempo_restante_segundos > 0) {
                this.tiempoRestantePorDocumento[doc.id_requisito_adjunto] = doc.tiempo_restante_segundos;
                hayDocumentosConTiempo = true;
            }
        }

        if (hayDocumentosConTiempo) {
            this.iniciarTemporizadores();
        }
    }

    /**
     * Carga los documentos de la postulación por separado.
     */
    cargarDocumentosPostulacion(idPostulacion: number) {
        this.subs.add(
            this.postulanteService.listarDocumentosPostulacion(idPostulacion).subscribe({
                next: (docs) => {
                    // Mapear al formato esperado por el componente
                    this.documentos = (docs || []).map(d => ({
                        id_requisito_adjunto: d.idRequisitoAdjunto,
                        id_tipo_requisito: d.idTipoRequisitoPostulacion || 0,
                        nombre_requisito: d.nombreRequisito || '',
                        descripcion_requisito: '',
                        tipo_documento_permitido: '',
                        nombre_archivo: d.nombreArchivo || '',
                        fecha_subida: d.fechaSubida || '',
                        estado: d.nombreEstado || 'PENDIENTE',
                        id_tipo_estado_requisito: 0,
                        observacion: d.observacion || '',
                        es_editable: d.nombreEstado?.toUpperCase() === 'OBSERVADO',
                        tiene_archivo: !!d.nombreArchivo
                    }));
                },
                error: (err) => {
                    console.error('Error al cargar documentos:', err);
                    this.documentos = [];
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

        // Validación de seguridad: bloquear si postulación rechazada
        if (this.postulacionRechazada) {
            this.mensajeReemplazo[idRequisito] = '✖ La postulación está rechazada. No es posible modificar documentos.';
            input.value = '';
            return;
        }

        // Validación de seguridad: verificar periodo de subsanación antes de subir
        if (!this.esPeriodoSubsanacion) {
            this.mensajeReemplazo[idRequisito] = '✖ ' + this.mensajePeriodoExpirado;
            input.value = ''; // Limpiar el input
            return;
        }

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
    getEtapaIcono(codigo: string): string {
        switch (codigo?.toUpperCase()) {
            case 'POSTULACION': return 'clipboard-check';
            case 'REVISION_REQUISITOS':
            case 'REVISION': return 'eye';
            case 'EVALUACION_MERITOS': return 'award';
            case 'RESULTADOS':
            case 'PUBLICACION_RESULTADOS': return 'flag';
            case 'EJECUCION': return 'users';
            default: return 'clock';
        }
    }

    getEtapaClase(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'FINALIZADA':
            case 'COMPLETADA': return 'etapa-completada';
            case 'EN CURSO':
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
            case 'CORREGIDO': return 'doc-estado corregido';
            default: return 'doc-estado pendiente';
        }
    }

    getEstadoDocumentoIcono(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'APROBADO': return 'check-circle';
            case 'RECHAZADO': return 'x-circle';
            case 'OBSERVADO': return 'alert-circle';
            case 'CORREGIDO': return 'upload';
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
            case 'APROBADA':
            case 'APTO': return 'status-pill aprobado';
            case 'RECHAZADO':
            case 'RECHAZADA':
            case 'NO_APTO': return 'status-pill rechazado';
            case 'EN_REVISION':
            case 'EN_EVALUACION': return 'status-pill info';
            case 'OBSERVADO':
            case 'OBSERVADA': return 'status-pill observado';
            default: return 'status-pill pendiente';
        }
    }

    // Verifica si la postulación está rechazada (bloquea todas las acciones)
    get postulacionRechazada(): boolean {
        if (this.esPostulacionRechazada) return true;
        const estado = this.postulacion?.estado_nombre?.toUpperCase() || '';
        return estado === 'RECHAZADA' || estado === 'RECHAZADO';
    }

    // Verifica si un documento es editable (considerando el estado de la postulación y el periodo)
    esDocumentoEditable(doc: DocumentoPostulacionDTO): boolean {
        // Si la postulación está rechazada, ningún documento es editable
        if (this.postulacionRechazada) {
            return false;
        }
        // Si no estamos en periodo de subsanación, no se puede editar
        if (!this.esPeriodoSubsanacion) {
            return false;
        }
        // De lo contrario, usar el flag es_editable del documento (estado OBSERVADO)
        return doc.es_editable === true;
    }

    /**
     * Verifica si un documento del detalle es editable (con validación de plazo 24h).
     */
    esDocumentoDetalleEditable(doc: DocumentoDetalleDTO): boolean {
        // Si la postulación está rechazada, ningún documento es editable
        if (this.postulacionRechazada) {
            return false;
        }
        // Si no estamos en periodo de subsanación, no se puede editar
        if (!this.esPeriodoSubsanacion) {
            return false;
        }
        // Si el plazo de 24h expiró, no se puede editar
        if (doc.plazo_expirado) {
            return false;
        }
        // De lo contrario, usar el flag es_editable del documento (estado OBSERVADO)
        return doc.es_editable === true;
    }

    /**
     * Formatea el tiempo restante en formato legible (HH:MM:SS).
     */
    formatearTiempoRestante(segundos: number | undefined): string {
        if (segundos === undefined || segundos === null || segundos <= 0) {
            return '00:00:00';
        }

        const horas = Math.floor(segundos / 3600);
        const minutos = Math.floor((segundos % 3600) / 60);
        const segs = segundos % 60;

        return `${this.padZero(horas)}:${this.padZero(minutos)}:${this.padZero(segs)}`;
    }

    /**
     * Obtiene el tiempo restante formateado para un documento.
     */
    getTiempoRestanteFormateado(idRequisito: number): string {
        const tiempo = this.tiempoRestantePorDocumento[idRequisito];
        return this.formatearTiempoRestante(tiempo);
    }

    /**
     * Indica si un documento tiene temporizador activo.
     */
    tieneTemporizadorActivo(doc: DocumentoDetalleDTO): boolean {
        return doc.estado_nombre?.toUpperCase() === 'OBSERVADO'
            && doc.tiempo_restante_segundos !== undefined
            && doc.tiempo_restante_segundos > 0
            && !doc.plazo_expirado;
    }

    private padZero(num: number): string {
        return num.toString().padStart(2, '0');
    }

    /**
     * Mensaje a mostrar cuando el periodo de subsanación ha expirado.
     */
    get mensajePeriodoExpirado(): string {
        return 'El periodo de corrección ha finalizado según el cronograma académico.';
    }

    /**
     * Mensaje a mostrar cuando el plazo de 24 horas ha expirado.
     */
    get mensajePlazo24hExpirado(): string {
        return 'El plazo de 24 horas para corregir este documento ha expirado.';
    }
}
