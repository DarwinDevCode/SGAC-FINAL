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
    LUCIDE_ICONS
} from 'lucide-angular';
import { ComisionService } from '../../../core/services/comision-service';
import { DecanoService } from '../../../core/services/decano-service';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { DocenteService } from '../../../core/services/docente-service';
import { ComisionIntegranteService } from '../../../core/services/comision-integrante-service';
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
                Save
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

    // Estado de datos
    convocatorias: ConvocatoriaDTO[] = [];
    comisiones: ComisionDTO[] = [];
    decanos: DecanoResponseDTO[] = [];
    coordinadores: CoordinadorResponseDTO[] = [];
    docentes: DocenteDTO[] = [];

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

    // Confirmación desactivar
    comisionADesactivar: ComisionDTO | null = null;
    desactivando = false;
    mensajeExito = '';

    ngOnInit(): void {
        this.decanoService.listarConvocatoriasActivas().subscribe({
            next: (data) => { this.convocatorias = data; this.loadingConvocatorias = false; },
            error: () => { this.loadingConvocatorias = false; },
        });

        // Cargar listas para integrantes
        this.decanoService.listarActivos().subscribe(res => {
            console.log('Decanos activos cargados:', res.length, res);
            this.decanos = res;
        });
        this.coordinadorService.listarActivos().subscribe(res => {
            console.log('Coordinadores activos cargados:', res.length, res);
            this.coordinadores = res;
        });
        this.docenteService.listarActivos().subscribe(res => {
            console.log('Docentes activos cargados:', res.length, res);
            this.docentes = res;
        });
    }

    seleccionarConvocatoria(): void {
        if (!this.idConvocatoriaSeleccionada) return;
        this.loadingComisiones = true;
        this.comisiones = [];
        this.comisionService.listarPorConvocatoria(this.idConvocatoriaSeleccionada).subscribe({
            next: (data) => { this.comisiones = data; this.loadingComisiones = false; },
            error: () => { this.loadingComisiones = false; },
        });
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

        this.idUsuarioDecano = null;

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
        const nombre = c.nombreAsignatura ?? 'Convocatoria';
        const periodo = c.nombrePeriodo ? ` (${c.nombrePeriodo})` : '';
        return nombre + periodo;
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
