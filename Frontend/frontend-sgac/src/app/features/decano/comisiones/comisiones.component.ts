import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
    LucideAngularModule,
    CheckCircle2,
    FileText,
    Plus,
    Loader2,
    Users,
    Pencil,
    XCircle,
    X,
    Lock,
    AlertTriangle,
    Save,
    LucideIconProvider,
    LUCIDE_ICONS,
    RefreshCw,
    ChevronLeft,
    FolderOpen
} from 'lucide-angular';
import { ComisionService } from '../../../core/services/comision-service';
import { DecanoService } from '../../../core/services/decano-service';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { DocenteService } from '../../../core/services/docente-service';
import { ComisionIntegranteService } from '../../../core/services/comision-integrante-service';
import { AuthService } from '../../../core/services/auth-service';
import { ComisionDTO, ComisionRequestDTO } from '../../../core/dto/comision';
import { ConvocatoriaDTO } from '../../../core/dto/convocatoria';
import { DecanoResponseDTO } from '../../../core/dto/decano';
import { CoordinadorResponseDTO } from '../../../core/dto/coordinador';
import { DocenteDTO } from '../../../core/dto/docente';
import { UsuarioComisionRequestDTO } from '../../../core/dto/usuario-comision-request';
import { forkJoin, of } from 'rxjs';

@Component({
    selector: 'app-decano-comisiones',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    providers: [
        {
            provide: LUCIDE_ICONS,
            multi: true,
            useValue: new LucideIconProvider({
                CheckCircle2,
                FileText,
                Plus,
                Loader2,
                Users,
                Pencil,
                XCircle,
                X,
                Lock,
                AlertTriangle,
                Save,
                RefreshCw,
                ChevronLeft,
                FolderOpen
            })
        }
    ],
    templateUrl: './comisiones.html',
    styleUrl: './comisiones.css',
})
export class ComisionesDecanoComponent implements OnInit {
    private comisionService = inject(ComisionService);
    private decanoService = inject(DecanoService);
    private coordinadorService = inject(CoordinadorService);
    private docenteService = inject(DocenteService);
    private integranteService = inject(ComisionIntegranteService);
    private authService = inject(AuthService);

    // Estado de datos
    convocatorias: ConvocatoriaDTO[] = [];
    comisiones: ComisionDTO[] = [];
    decanos: DecanoResponseDTO[] = [];
    coordinadores: CoordinadorResponseDTO[] = [];
    docentes: DocenteDTO[] = [];

    // Control de vista
    tabActivo: 'sin-asignar' | 'asignadas' = 'sin-asignar';
    vistaDetalle = false;
    convocatoriaDetalle: ConvocatoriaDTO | null = null;

    idConvocatoriaSeleccionada: number | null = null;
    loadingConvocatorias = true;
    loadingComisiones = false;

    // Modal
    modalAbierto = false;
    modoEdicion = false;
    guardando = false;
    errorModal = '';
    comisionEditar: ComisionDTO | null = null;

    // Formulario del modal
    form: ComisionRequestDTO = {
        idConvocatoria: 0,
        nombreComision: '',
        fechaConformacion: '',
        activo: true,
    };

    // Integrantes
    idUsuarioDecano: number | null = null;
    idUsuarioCoordinador: number | null = null;
    idUsuarioDocente: number | null = null;
    decanoLogueado: DecanoResponseDTO | null = null;

    // Confirmación desactivar
    comisionADesactivar: ComisionDTO | null = null;
    desactivando = false;
    mensajeExito = '';

    ngOnInit(): void {
        const currentUser = this.authService.getUser();

        forkJoin({
            decanos: this.decanoService.listarActivos(),
            convocatorias: this.decanoService.listarConvocatoriasActivas()
        }).subscribe({
            next: (res) => {
                this.decanos = res.decanos;
                if (currentUser) {
                    this.decanoLogueado = this.decanos.find(d => d.idUsuario === currentUser.idUsuario) || null;
                }

                if (this.decanoLogueado && this.decanoLogueado.idFacultad) {
                    this.convocatorias = res.convocatorias.filter(c => c.idFacultad === this.decanoLogueado?.idFacultad);
                } else {
                    this.convocatorias = res.convocatorias;
                }

                // Cargar estado de comisiones para cada convocatoria de forma asíncrona
                if (this.convocatorias.length > 0) {
                    this.convocatorias.forEach(c => {
                        const idConv = c.idConvocatoria;
                        if (idConv !== undefined && idConv !== null) {
                            this.comisionService.listarPorConvocatoria(idConv).subscribe(comisiones => {
                                // Cualquier miembro activo
                                c._tieneComisiones = comisiones && comisiones.length > 0 && comisiones.some(com => com.activo);
                            });
                        }
                    });
                }

                this.loadingConvocatorias = false;
            },
            error: () => {
                this.loadingConvocatorias = false;
            }
        });

        this.coordinadorService.listarActivos().subscribe(res => this.coordinadores = res);
        this.docenteService.listarActivos().subscribe(res => this.docentes = res);
    }

    get convocatoriasFiltradas(): ConvocatoriaDTO[] {
        if (this.tabActivo === 'sin-asignar') {
            return this.convocatorias.filter(c => !c._tieneComisiones);
        } else {
            return this.convocatorias.filter(c => c._tieneComisiones);
        }
    }

    get contadorSinAsignar(): number {
        return this.convocatorias.filter(c => !c._tieneComisiones).length;
    }

    get contadorAsignadas(): number {
        return this.convocatorias.filter(c => c._tieneComisiones).length;
    }

    cambiarTab(tab: 'sin-asignar' | 'asignadas'): void {
        this.tabActivo = tab;
    }

    seleccionarConvocatoria(): void {
        if (!this.idConvocatoriaSeleccionada) return;
        this.loadingComisiones = true;
        this.comisiones = [];
        this.comisionService.listarPorConvocatoria(this.idConvocatoriaSeleccionada).subscribe({
            next: (data) => {
                this.comisiones = data;
                this.loadingComisiones = false;

                // Marcar en la lista original que ya tiene comisiones (para la UI)
                const conv = this.convocatorias.find(c => c.idConvocatoria === this.idConvocatoriaSeleccionada);
                if (conv) {
                    conv._tieneComisiones = data && data.length > 0;
                }
            },
            error: () => { this.loadingComisiones = false; },
        });
    }

    verDetalle(convocatoria: ConvocatoriaDTO): void {
        this.convocatoriaDetalle = convocatoria;
        this.idConvocatoriaSeleccionada = convocatoria.idConvocatoria || null;
        this.vistaDetalle = true;
        this.seleccionarConvocatoria();
    }

    volverALista(): void {
        this.vistaDetalle = false;
        this.convocatoriaDetalle = null;
        this.idConvocatoriaSeleccionada = null;
        this.comisiones = [];
    }

    abrirModalCrear(): void {
        this.modoEdicion = false;
        this.comisionEditar = null;
        this.errorModal = '';
        this.form = {
            idConvocatoria: this.idConvocatoriaSeleccionada ?? 0,
            nombreComision: '',
            fechaConformacion: new Date().toISOString().split('T')[0],
            activo: true,
        };

        this.idUsuarioDecano = this.decanoLogueado ? this.decanoLogueado.idUsuario : null;

        // Auto-asignación de Coordinador y Docente desde la convocatoria
        const convocatoriaSeleccionada = this.convocatorias.find(c => c.idConvocatoria === this.idConvocatoriaSeleccionada);
        if (convocatoriaSeleccionada) {
            this.idUsuarioCoordinador = convocatoriaSeleccionada.idUsuarioCoordinador || null;
            this.idUsuarioDocente = convocatoriaSeleccionada.idUsuarioDocente || null;
            console.log('Preseleccionando integrantes:', { coordinador: this.idUsuarioCoordinador, docente: this.idUsuarioDocente });
        } else {
            this.idUsuarioCoordinador = null;
            this.idUsuarioDocente = null;
        }

        this.modalAbierto = true;
    }

    abrirModalEditar(c: ComisionDTO): void {
        this.modoEdicion = true;
        this.comisionEditar = c;
        this.errorModal = '';
        this.form = {
            idConvocatoria: c.idConvocatoria,
            nombreComision: c.nombreComision,
            fechaConformacion: c.fechaConformacion ?? new Date().toISOString().split('T')[0],
            activo: c.activo,
        };
        this.modalAbierto = true;
    }

    cerrarModal(): void {
        this.modalAbierto = false;
        this.errorModal = '';
    }

    guardar(): void {
        if (!this.form.nombreComision.trim()) {
            this.errorModal = 'El nombre de la comisión es obligatorio.';
            return;
        }

        if (!this.modoEdicion) {
            if (!this.idUsuarioDecano || !this.idUsuarioCoordinador || !this.idUsuarioDocente) {
                console.error('Error de validación de integrantes:', {
                    decano: this.idUsuarioDecano,
                    coordinador: this.idUsuarioCoordinador,
                    docente: this.idUsuarioDocente
                });

                if (!this.idUsuarioDecano) {
                    this.errorModal = 'Debes seleccionar al Decano para conformar la comisión.';
                } else {
                    this.errorModal = 'La convocatoria seleccionada no tiene un Coordinador o Docente asociado. Por favor, verifica la convocatoria original.';
                }
                return;
            }
        }

        this.guardando = true;
        this.errorModal = '';

        if (this.modoEdicion && this.comisionEditar) {
            this.comisionService.actualizar(this.comisionEditar.idComisionSeleccion, this.form).subscribe({
                next: () => this.finalizarGuardado('Comisión actualizada.'),
                error: (err) => this.manejarError(err)
            });
        } else {
            this.comisionService.crear(this.form).subscribe({
                next: (nuevaComision) => {
                    // Asignar los 3 integrantes
                    const requistos: UsuarioComisionRequestDTO[] = [
                        { idComisionSeleccion: nuevaComision.idComisionSeleccion, idUsuario: this.idUsuarioDecano!, rolIntegrante: 'DECANO' },
                        { idComisionSeleccion: nuevaComision.idComisionSeleccion, idUsuario: this.idUsuarioCoordinador!, rolIntegrante: 'COORDINADOR' },
                        { idComisionSeleccion: nuevaComision.idComisionSeleccion, idUsuario: this.idUsuarioDocente!, rolIntegrante: 'DOCENTE' }
                    ];

                    forkJoin(requistos.map(r => this.integranteService.asignar(r))).subscribe({
                        next: () => this.finalizarGuardado('Comisión y sus 3 integrantes asignados correctamente.'),
                        error: (err) => this.manejarError(err)
                    });
                },
                error: (err) => this.manejarError(err)
            });
        }
    }

    private finalizarGuardado(mensaje: string) {
        this.guardando = false;
        this.cerrarModal();
        this.mensajeExito = mensaje;
        setTimeout(() => this.mensajeExito = '', 4000);
        if (this.idConvocatoriaSeleccionada) this.seleccionarConvocatoria();
    }

    private manejarError(err: any) {
        this.guardando = false;
        this.errorModal = 'Error: ' + (err?.error || err?.message || 'Error inesperado.');
    }

    confirmarDesactivar(c: ComisionDTO): void {
        this.comisionADesactivar = c;
    }

    cancelarDesactivar(): void {
        this.comisionADesactivar = null;
    }

    ejecutarDesactivar(): void {
        if (!this.comisionADesactivar) return;
        this.desactivando = true;
        this.comisionService.desactivar(this.comisionADesactivar.idComisionSeleccion).subscribe({
            next: () => {
                this.desactivando = false;
                this.comisionADesactivar = null;
                this.mensajeExito = 'Comisión desactivada correctamente.';
                setTimeout(() => this.mensajeExito = '', 3000);
                if (this.idConvocatoriaSeleccionada) this.seleccionarConvocatoria();
            },
            error: () => { this.desactivando = false; },
        });
    }

    nombreConvocatoria(c: ConvocatoriaDTO): string {
        const materia = c.nombreAsignatura ?? 'Convocatoria';
        const carrera = c.nombreCarrera ? ` - ${c.nombreCarrera}` : '';
        const periodo = c.nombrePeriodo ? ` (${c.nombrePeriodo})` : '';
        return materia + carrera + periodo;
    }

    getNombreDecanoStr(): string {
        if (!this.idUsuarioDecano) return '— No asignado —';
        const d = this.decanos.find(dec => dec.idUsuario === this.idUsuarioDecano);
        return d ? d.nombreCompletoUsuario : 'Desconocido';
    }

    formatearFecha(fecha: string | number[] | undefined): string | null {
        if (!fecha) return null;
        if (Array.isArray(fecha)) {
            // Asumiendo formato [year, month, day]
            if (fecha.length >= 3) {
                const dateObj = new Date(fecha[0], fecha[1] - 1, fecha[2]);
                return dateObj.toISOString();
            }
            return null;
        }
        return fecha;
    }

    getNombreCoordinadorStr(): string {
        if (!this.idUsuarioCoordinador) return '— No asignado —';
        const c = this.coordinadores.find(coord => coord.idUsuario === this.idUsuarioCoordinador);
        return c ? c.nombreCompletoUsuario : 'Desconocido';
    }

    getNombreDocenteStr(): string {
        if (!this.idUsuarioDocente) return '— No asignado —';
        const d = this.docentes.find(doc => doc.idUsuario === this.idUsuarioDocente);
        return d ? d.nombreCompletoUsuario : 'Desconocido';
    }
}
