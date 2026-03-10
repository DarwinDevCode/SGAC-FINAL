import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
    LucideAngularModule, LUCIDE_ICONS, LucideIconProvider,
    CheckCircle, AlertCircle, Loader2, Clock, CheckSquare, X,
    Paperclip, FileText, Eye, MessageSquare, Check, Users, Calendar,
    ThumbsUp, ThumbsDown, Edit3, Download, ChevronLeft, RefreshCw,
    AlertTriangle, Search, Filter, XCircle, Inbox, UserCheck, CalendarCheck
} from 'lucide-angular';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth-service';
import { EvaluacionPostulacionService } from '../../../core/services/evaluacion-postulacion-service';
import {
    PostulacionListadoCoordinador,
    DetallePostulacionCoordinador,
    DocumentoEvaluacion,
    EvaluarDocumentoRequest,
    DictaminarPostulacionRequest,
    AsignarComisionRequest
} from '../../../core/dto/evaluacion-postulacion';
import { ComisionService } from '../../../core/services/comision-service';
import { ComisionDTO } from '../../../core/dto/comision';

@Component({
    selector: 'app-coordinador-validaciones',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, LucideAngularModule],
    templateUrl: './validaciones.html',
    styleUrl: './validaciones.css',
    providers: [
        {
            provide: LUCIDE_ICONS,
            multi: true,
            useValue: new LucideIconProvider({
                CheckCircle, AlertCircle, Loader2, Clock, CheckSquare, X,
                Paperclip, FileText, Eye, MessageSquare, Check, Users, Calendar,
                ThumbsUp, ThumbsDown, Edit3, Download, ChevronLeft, RefreshCw,
                AlertTriangle, Search, Filter, XCircle, Inbox, UserCheck, CalendarCheck
            })
        }
    ]
})
export class ValidacionesComponent implements OnInit, OnDestroy {
    private authService = inject(AuthService);
    private evaluacionService = inject(EvaluacionPostulacionService);
    private comisionService = inject(ComisionService);
    private subs = new Subscription();

    // Estado general
    postulaciones: PostulacionListadoCoordinador[] = [];
    loading = true;
    errorMensaje = '';
    successMensaje = '';
    idUsuario: number | null = null;

    // Filtros
    filtroEstado = 'TODOS';
    filtroBusqueda = '';

    // Tabs
    tabActivo: 'requiere-atencion' | 'todas' | 'aprobados' = 'requiere-atencion';

    // Vista detalle
    vistaDetalle = false;
    detallePostulacion: DetallePostulacionCoordinador | null = null;
    loadingDetalle = false;
    postulacionSeleccionadaId: number | null = null;

    // Evaluar documento
    documentoEvaluando: DocumentoEvaluacion | null = null;
    accionDocumento: 'VALIDAR' | 'OBSERVAR' | 'RECHAZAR' | null = null;
    observacionDocumento = '';
    loadingEvaluacion = false;

    // Modal dictamen
    mostrarModalDictamen = false;
    accionDictamen: 'APROBAR' | 'RECHAZAR' | null = null;
    observacionDictamen = '';
    loadingDictamen = false;

    // Modal Asignación de Comisión
    mostrarModalAsignacion = false;
    postulacionAAsignar: PostulacionListadoCoordinador | null = null;
    comisionesDisponibles: ComisionDTO[] = [];
    loadingComisiones = false;
    loadingAsignacion = false;
    formAsignacion: Partial<AsignarComisionRequest> = {
        temaExposicion: '',
        fechaEvaluacion: new Date().toISOString().split('T')[0],
        horaInicio: '09:00',
        horaFin: '10:00',
        lugar: ''
    };

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (user) {
            this.idUsuario = user.idUsuario;
            this.cargarPostulaciones();
        }
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarPostulaciones(): void {
        if (!this.idUsuario) return;

        this.loading = true;
        this.errorMensaje = '';

        this.subs.add(
            this.evaluacionService.listarPostulaciones(this.idUsuario).subscribe({
                next: (lista) => {
                    this.postulaciones = lista;
                    this.loading = false;
                },
                error: (err) => {
                    console.error('Error al cargar postulaciones:', err);
                    this.errorMensaje = err.error?.mensaje || 'Error al cargar las postulaciones';
                    this.loading = false;
                }
            })
        );
    }

    // Filtrado de postulaciones
    get postulacionesFiltradas(): PostulacionListadoCoordinador[] {
        let lista = this.postulaciones;

        // Filtro por tab
        if (this.tabActivo === 'requiere-atencion') {
            lista = lista.filter(p => p.requiere_atencion);
        } else if (this.tabActivo === 'aprobados') {
            lista = lista.filter(p => p.estado_codigo === 'APROBADA' || p.estado_codigo === 'EN_EVALUACION');
        }

        // Filtro por estado
        if (this.filtroEstado !== 'TODOS') {
            lista = lista.filter(p => p.estado_codigo === this.filtroEstado);
        }

        // Filtro por búsqueda
        if (this.filtroBusqueda.trim()) {
            const busqueda = this.filtroBusqueda.toLowerCase();
            lista = lista.filter(p =>
                p.nombre_estudiante.toLowerCase().includes(busqueda) ||
                p.matricula.toLowerCase().includes(busqueda) ||
                p.nombre_asignatura.toLowerCase().includes(busqueda)
            );
        }

        return lista;
    }

    get estadosUnicos(): string[] {
        return [...new Set(this.postulaciones.map(p => p.estado_codigo))].filter(Boolean);
    }

    get contadorRequierenAtencion(): number {
        return this.postulaciones.filter(p => p.requiere_atencion).length;
    }

    get contadorAprobados(): number {
        return this.postulaciones.filter(p => p.estado_codigo === 'APROBADA' || p.estado_codigo === 'EN_EVALUACION').length;
    }

    cambiarTab(tab: 'requiere-atencion' | 'todas' | 'aprobados'): void {
        this.tabActivo = tab;
    }

    // Ver detalle de postulación
    verDetalle(postulacion: PostulacionListadoCoordinador): void {
        if (!this.idUsuario) return;

        this.postulacionSeleccionadaId = postulacion.id_postulacion;
        this.loadingDetalle = true;
        this.vistaDetalle = true;
        this.errorMensaje = '';

        // Primero iniciar revisión (cambiar estado si es necesario)
        this.subs.add(
            this.evaluacionService.iniciarRevision(this.idUsuario, postulacion.id_postulacion).subscribe({
                next: (resp) => {
                    if (resp.cambio_realizado) {
                        const idx = this.postulaciones.findIndex(p => p.id_postulacion === postulacion.id_postulacion);
                        if (idx >= 0) {
                            this.postulaciones[idx].estado_codigo = resp.estado_actual || 'EN_REVISION';
                            this.postulaciones[idx].estado_nombre = 'En Revisión';
                        }
                    }
                    this.cargarDetallePostulacion(postulacion.id_postulacion);
                },
                error: () => {
                    this.cargarDetallePostulacion(postulacion.id_postulacion);
                }
            })
        );
    }

    cargarDetallePostulacion(idPostulacion: number): void {
        if (!this.idUsuario) return;

        this.subs.add(
            this.evaluacionService.obtenerDetallePostulacion(this.idUsuario, idPostulacion).subscribe({
                next: (detalle) => {
                    this.detallePostulacion = detalle;
                    this.loadingDetalle = false;
                },
                error: (err) => {
                    console.error('Error al cargar detalle:', err);
                    this.errorMensaje = err.error?.mensaje || 'Error al cargar el detalle de la postulación';
                    this.loadingDetalle = false;
                }
            })
        );
    }

    volverALista(): void {
        this.vistaDetalle = false;
        this.detallePostulacion = null;
        this.postulacionSeleccionadaId = null;
        this.cargarPostulaciones();
    }

    // Visualizar documento
    visualizarDocumento(doc: DocumentoEvaluacion): void {
        if (!this.idUsuario || !doc.tiene_archivo) return;

        this.subs.add(
            this.evaluacionService.visualizarDocumento(this.idUsuario, doc.id_requisito_adjunto).subscribe({
                next: (blob) => {
                    const url = window.URL.createObjectURL(blob);
                    window.open(url, '_blank');
                },
                error: (err) => {
                    console.error('Error al visualizar documento:', err);
                    this.errorMensaje = 'Error al visualizar el documento';
                    setTimeout(() => this.errorMensaje = '', 3000);
                }
            })
        );
    }

    descargarDocumento(doc: DocumentoEvaluacion): void {
        if (!this.idUsuario || !doc.tiene_archivo) return;

        this.subs.add(
            this.evaluacionService.descargarDocumento(this.idUsuario, doc.id_requisito_adjunto).subscribe({
                next: (blob) => {
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = doc.nombre_archivo || 'documento.pdf';
                    a.click();
                    window.URL.revokeObjectURL(url);
                },
                error: (err) => {
                    console.error('Error al descargar documento:', err);
                    this.errorMensaje = 'Error al descargar el documento';
                    setTimeout(() => this.errorMensaje = '', 3000);
                }
            })
        );
    }

    // Evaluar documento
    iniciarEvaluacionDocumento(doc: DocumentoEvaluacion, accion: 'VALIDAR' | 'OBSERVAR' | 'RECHAZAR'): void {
        this.documentoEvaluando = doc;
        this.accionDocumento = accion;
        this.observacionDocumento = '';
    }

    cancelarEvaluacionDocumento(): void {
        this.documentoEvaluando = null;
        this.accionDocumento = null;
        this.observacionDocumento = '';
    }

    confirmarEvaluacionDocumento(): void {
        if (!this.idUsuario || !this.documentoEvaluando || !this.accionDocumento) return;

        if (this.accionDocumento === 'OBSERVAR' && !this.observacionDocumento.trim()) {
            this.errorMensaje = 'Debe ingresar una observación para el documento';
            setTimeout(() => this.errorMensaje = '', 3000);
            return;
        }

        this.loadingEvaluacion = true;

        const request: EvaluarDocumentoRequest = {
            id_requisito_adjunto: this.documentoEvaluando.id_requisito_adjunto,
            accion: this.accionDocumento,
            observacion: this.accionDocumento === 'OBSERVAR' ? this.observacionDocumento : undefined
        };

        this.subs.add(
            this.evaluacionService.evaluarDocumento(this.idUsuario, request).subscribe({
                next: (resp) => {
                    this.loadingEvaluacion = false;
                    if (resp.exito) {
                        this.successMensaje = resp.mensaje;
                        if (this.postulacionSeleccionadaId) {
                            this.cargarDetallePostulacion(this.postulacionSeleccionadaId);
                        }
                        this.cancelarEvaluacionDocumento();
                    } else {
                        this.errorMensaje = resp.mensaje;
                    }
                    setTimeout(() => {
                        this.successMensaje = '';
                        this.errorMensaje = '';
                    }, 4000);
                },
                error: (err) => {
                    this.loadingEvaluacion = false;
                    this.errorMensaje = err.error?.mensaje || 'Error al evaluar el documento';
                    setTimeout(() => this.errorMensaje = '', 4000);
                }
            })
        );
    }

    // Dictaminar postulación
    abrirModalDictamen(accion: 'APROBAR' | 'RECHAZAR'): void {
        this.accionDictamen = accion;
        this.observacionDictamen = '';
        this.mostrarModalDictamen = true;
    }

    cerrarModalDictamen(): void {
        this.mostrarModalDictamen = false;
        this.accionDictamen = null;
        this.observacionDictamen = '';
    }

    confirmarDictamen(): void {
        if (!this.idUsuario || !this.detallePostulacion || !this.accionDictamen) return;

        if (this.accionDictamen === 'RECHAZAR' && !this.observacionDictamen.trim()) {
            this.errorMensaje = 'Debe ingresar un motivo para rechazar la postulación';
            setTimeout(() => this.errorMensaje = '', 3000);
            return;
        }

        this.loadingDictamen = true;

        const request: DictaminarPostulacionRequest = {
            id_postulacion: this.detallePostulacion.postulacion.id_postulacion,
            accion: this.accionDictamen,
            observacion: this.accionDictamen === 'RECHAZAR' ? this.observacionDictamen : undefined
        };

        this.subs.add(
            this.evaluacionService.dictaminarPostulacion(this.idUsuario, request).subscribe({
                next: (resp) => {
                    this.loadingDictamen = false;
                    if (resp.exito) {
                        this.successMensaje = resp.mensaje;
                        this.cerrarModalDictamen();
                        this.volverALista();
                    } else {
                        this.errorMensaje = resp.mensaje;
                    }
                    setTimeout(() => {
                        this.successMensaje = '';
                        this.errorMensaje = '';
                    }, 4000);
                },
                error: (err) => {
                    this.loadingDictamen = false;
                    this.errorMensaje = err.error?.mensaje || 'Error al dictaminar la postulación';
                    setTimeout(() => this.errorMensaje = '', 4000);
                }
            })
        );
    }

    // Helpers
    getEstadoClase(estado: string): string {
        const clases: { [key: string]: string } = {
            'PENDIENTE': 'status-pendiente',
            'EN_REVISION': 'status-revision',
            'OBSERVADA': 'status-observada',
            'CORREGIDA': 'status-corregida',
            'APROBADA': 'status-aprobada',
            'EN_EVALUACION': 'status-evaluacion',
            'RECHAZADA': 'status-rechazada'
        };
        return clases[estado] || 'status-default';
    }

    getDocumentoEstadoClase(estado: string): string {
        const estadoUpper = estado?.toUpperCase() || '';
        if (estadoUpper.includes('APROBADO') || estadoUpper.includes('VALIDADO')) return 'doc-aprobado';
        if (estadoUpper.includes('OBSERVADO')) return 'doc-observado';
        if (estadoUpper.includes('RECHAZADO')) return 'doc-rechazado';
        if (estadoUpper.includes('CORREGIDO')) return 'doc-corregido';
        return 'doc-pendiente';
    }

    puedeEvaluarDocumento(doc: DocumentoEvaluacion): boolean {
        const estado = doc.estado_codigo?.toUpperCase() || '';
        return estado.includes('PENDIENTE') || estado.includes('CORREGIDO');
    }

    formatearFecha(fecha: string): string {
        if (!fecha) return '—';
        const date = new Date(fecha);
        return date.toLocaleDateString('es-EC', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }

    // Modal de asiganción
    abrirModalAsignacion(postulacion: PostulacionListadoCoordinador): void {
        this.postulacionAAsignar = postulacion;
        this.mostrarModalAsignacion = true;
        this.errorMensaje = '';
        this.successMensaje = '';
        this.formAsignacion = {
            idPostulacion: postulacion.id_postulacion,
            idComisionSeleccion: undefined,
            temaExposicion: '',
            fechaEvaluacion: new Date().toISOString().split('T')[0],
            horaInicio: '09:00',
            horaFin: '10:00',
            lugar: ''
        };
        this.cargarComisionesDisponibles(postulacion.id_convocatoria);
    }

    cargarComisionesDisponibles(idConvocatoria: number): void {
        this.loadingComisiones = true;
        this.subs.add(
            this.comisionService.listarPorConvocatoria(idConvocatoria).subscribe({
                next: (res) => {
                    this.comisionesDisponibles = res.filter(c => c.activo);
                    this.loadingComisiones = false;
                },
                error: (err) => {
                    this.errorMensaje = 'No se pudieron cargar las comisiones para esta asignatura.';
                    this.loadingComisiones = false;
                    setTimeout(() => this.errorMensaje = '', 3000);
                }
            })
        );
    }

    cerrarModalAsignacion(): void {
        this.mostrarModalAsignacion = false;
        this.postulacionAAsignar = null;
        this.formAsignacion = {};
    }

    confirmarAsignacion(): void {
        if (!this.formAsignacion.idComisionSeleccion || !this.formAsignacion.temaExposicion || !this.formAsignacion.lugar) {
            this.errorMensaje = 'Por favor complete todos los campos obligatorios (*).';
            setTimeout(() => this.errorMensaje = '', 3000);
            return;
        }

        this.loadingAsignacion = true;
        const request = this.formAsignacion as AsignarComisionRequest;

        this.subs.add(
            this.evaluacionService.asignarComision(request).subscribe({
                next: (resp) => {
                    this.loadingAsignacion = false;
                    this.successMensaje = 'Comisión asignada exitosamente.';
                    this.cerrarModalAsignacion();
                    // Refrescar toda la lista para asegurar estados consistentes
                    this.cargarPostulaciones();
                    setTimeout(() => this.successMensaje = '', 3500);
                },
                error: (err) => {
                    this.loadingAsignacion = false;
                    this.errorMensaje = err.error?.mensaje || 'Error al asignar la comisión.';
                    setTimeout(() => this.errorMensaje = '', 4000);
                }
            })
        );
    }
}
