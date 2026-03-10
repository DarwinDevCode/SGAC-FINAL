import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, CheckCircle, AlertCircle, Loader2, Clock, CheckSquare, X, Paperclip, FileText, Eye, MessageSquare, Check, Users, Calendar } from 'lucide-angular';
import { Subscription, switchMap } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO, RequisitoAdjuntoResponseDTO } from '../../../core/dto/postulacion';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ComisionService } from '../../../core/services/comision-service';
import { ComisionDTO } from '../../../core/dto/comision';
import { EvaluacionOposicionService, AsignarComisionRequest } from '../../../core/services/evaluacion-oposicion-service';

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
            useValue: new LucideIconProvider({ CheckCircle, AlertCircle, Loader2, Clock, CheckSquare, X, Paperclip, FileText, Eye, MessageSquare, Check, Users, Calendar })
        }
    ]
})
export class ValidacionesComponent implements OnInit, OnDestroy {
    private coordinadorService = inject(CoordinadorService);
    private authService = inject(AuthService);
    private http = inject(HttpClient);
    private comisionService = inject(ComisionService);
    private evaluacionService = inject(EvaluacionOposicionService);
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
    documentos: RequisitoAdjuntoResponseDTO[] = [];
    loadingDocs = false;

    // P7: Observar un documento individual
    docObservandoId: number | null = null;
    docObservacion = '';
    guardandoObs = false;

    // Modal Asignar Comisión
    mostrarModalAsignacion = false;
    postulacionParaAsignar: PostulacionResponseDTO | null = null;
    comisionesDisponibles: ComisionDTO[] = [];
    loadingComisiones = false;
    asignacionForm = {
        idComisionSeleccion: null as number | null,
        temaExposicion: '',
        fechaEvaluacion: '',
        horaInicio: '',
        horaFin: '',
        lugar: ''
    };
    asignando = false;

    // Tabs
    tabActivo: 'pendientes' | 'en-evaluacion' = 'pendientes';
    enEvaluacion: PostulacionResponseDTO[] = [];
    loadingEval = false;
    idCarreraActual: number | null = null;

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
                    this.idCarreraActual = coord.idCarrera ?? 0;
                    return this.coordinadorService.listarPendientesPorCarrera(coord.idCarrera ?? 0);
                })
            ).subscribe({
                next: (lista) => {
                    this.postulantes = lista;
                    this.loading = false;
                    // load EN_EVALUACION tab in background
                    this.cargarEnEvaluacion();
                },
                error: (err: HttpErrorResponse) => {
                    console.error(err.error?.data?.message || err.error?.message || err.message || 'Error al cargar postulaciones:');
                    this.errorMensaje = 'Error al cargar postulaciones.';
                    this.loading = false;
                }
            })
        );
    }

    cargarEnEvaluacion() {
        if (!this.idCarreraActual) return;
        this.loadingEval = true;
        this.subs.add(
            this.coordinadorService.listarEnEvaluacionPorCarrera(this.idCarreraActual).subscribe({
                next: (lista) => {
                    this.enEvaluacion = lista;
                    this.loadingEval = false;
                },
                error: () => { this.loadingEval = false; }
            })
        );
    }

    cambiarTab(tab: 'pendientes' | 'en-evaluacion') {
        this.tabActivo = tab;
        if (tab === 'en-evaluacion') this.cargarEnEvaluacion();
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

    verDocumento(event: Event, idRequisito: number) {
        event.preventDefault();
        this.subs.add(
            this.http.get(`http://localhost:8080/api/requisitos-adjuntos/descargar/${idRequisito}`, {
                responseType: 'blob'
            }).subscribe({
                next: (blob) => {
                    const url = window.URL.createObjectURL(blob);
                    window.open(url, '_blank');
                    // Opcional: Para liberarlo luego, window.URL.revokeObjectURL(url) pero si es _blank el GC del nuevo tab lo limpia.
                },
                error: (err: HttpErrorResponse) => {
                    console.error('Error al descargar documento:', err);
                    alert('No tiene los permisos suficientes o el documento no existe.');
                }
            })
        );
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
                error: (err: HttpErrorResponse) => {
                    console.error(err.error?.data?.message || err.error?.message || err.message || 'Error al cambiar estado de postulación:');
                    this.errorMensaje = 'Error al actualizar el estado.';
                    this.loadingAccion = false;
                    setTimeout(() => this.errorMensaje = '', 4000);
                }
            })
        );
    }

    /** P7: Marcar un documento individual como OBSERVADO con una observación */
    observarDocumento(doc: RequisitoAdjuntoResponseDTO) {
        if (!this.docObservacion.trim()) return;
        this.guardandoObs = true;
        // id_tipo_estado_requisito = 3 es el estado OBSERVADO (ajustar si difiere en tu BD)
        const ID_ESTADO_OBSERVADO = 3;
        const API_REQUISITOS = 'http://localhost:8080/api/requisitos-adjuntos';
        this.subs.add(
            this.http.put(`${API_REQUISITOS}/${doc.idRequisitoAdjunto}`, null, {
                params: { idTipoEstadoRequisito: ID_ESTADO_OBSERVADO, observacion: this.docObservacion },
                responseType: 'json'
            }).subscribe({
                next: (updated: any) => {
                    doc.nombreEstado = updated.nombreEstado ?? 'OBSERVADO';
                    doc.observacion = this.docObservacion;
                    this.docObservandoId = null;
                    this.docObservacion = '';
                    this.guardandoObs = false;
                },
                error: () => {
                    this.guardandoObs = false;
                    alert('Error al guardar la observación.');
                }
            })
        );
    }

    /** Abrir el modal para asignar una comisión a una postulación EN_EVALUACION */
    abrirModalAsignacion(p: PostulacionResponseDTO) {
        this.postulacionParaAsignar = p;
        this.asignacionForm = {
            idComisionSeleccion: null,
            temaExposicion: '',
            fechaEvaluacion: new Date().toISOString().split('T')[0],
            horaInicio: '09:00',
            horaFin: '10:00',
            lugar: ''
        };
        this.mostrarModalAsignacion = true;
        this.comisionesDisponibles = [];

        // Fetch comisiones for the convocatoria
        if (p.idConvocatoria) {
            this.loadingComisiones = true;
            this.subs.add(
                this.comisionService.listarPorConvocatoria(p.idConvocatoria).subscribe({
                    next: (coms) => {
                        this.comisionesDisponibles = coms.filter(c => c.activo);
                        this.loadingComisiones = false;
                    },
                    error: () => { this.loadingComisiones = false; }
                })
            );
        }
    }

    cerrarModalAsignacion() {
        this.mostrarModalAsignacion = false;
        this.postulacionParaAsignar = null;
    }

    confirmarAsignacion() {
        if (!this.postulacionParaAsignar?.idPostulacion) return;
        if (!this.asignacionForm.idComisionSeleccion) {
            alert('Debes seleccionar una comisión.');
            return;
        }
        if (!this.asignacionForm.temaExposicion || !this.asignacionForm.fechaEvaluacion || !this.asignacionForm.lugar) {
            alert('Por favor completa todos los campos requeridos.');
            return;
        }
        this.asignando = true;

        const request: AsignarComisionRequest = {
            idPostulacion: this.postulacionParaAsignar.idPostulacion,
            idComisionSeleccion: this.asignacionForm.idComisionSeleccion,
            temaExposicion: this.asignacionForm.temaExposicion,
            fechaEvaluacion: this.asignacionForm.fechaEvaluacion,
            horaInicio: this.asignacionForm.horaInicio + ':00',
            horaFin: this.asignacionForm.horaFin + ':00',
            lugar: this.asignacionForm.lugar
        };

        this.subs.add(
            this.evaluacionService.asignarComision(request).subscribe({
                next: () => {
                    this.successMensaje = '¡Comisión asignada correctamente al postulante!';
                    this.asignando = false;

                    if (this.postulacionParaAsignar) {
                        this.postulacionParaAsignar.comisionAsignada = true;
                    }

                    this.cerrarModalAsignacion();
                    setTimeout(() => this.successMensaje = '', 4000);
                },
                error: (err: HttpErrorResponse) => {
                    this.asignando = false;
                    this.errorMensaje = 'Error al asignar la comisión: ' + (err.error || err.message);
                    setTimeout(() => this.errorMensaje = '', 5000);
                }
            })
        );
    }
}

