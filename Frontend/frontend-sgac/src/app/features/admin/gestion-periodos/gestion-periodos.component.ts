import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { PeriodoAcademicoService } from '../../../core/services/periodo-academico-service';
import { PeriodoAcademicoDTO } from '../../../core/dto/periodo-academico';

@Component({
    selector: 'app-gestion-periodos',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    templateUrl: './gestion-periodos.component.html',
    styleUrl: './gestion-periodos.component.css'
})
export class GestionPeriodosComponent implements OnInit {
    periodoService = inject(PeriodoAcademicoService);

    periodos: PeriodoAcademicoDTO[] = [];
    modalAbierto = false;
    modalImportarAbierto = false;
    modoEdicion = false;

    formulario: Partial<PeriodoAcademicoDTO> = {
        nombrePeriodo: '',
        fechaInicio: '',
        fechaFin: '',
        estado: 'PLANIFICADO'
    };

    importarConfig = {
        idDestino: null as number | null,
        idFuente: null as number | null
    };

    ngOnInit() {
        this.cargarPeriodos();
    }

    cargarPeriodos() {
        this.periodoService.listarTodos().subscribe({
            next: (res) => {
                this.periodos = res;
            },
            error: (err) => console.error(err)
        });
    }

    abrirModalCrear() {
        this.modoEdicion = false;
        this.formulario = { nombrePeriodo: '', fechaInicio: '', fechaFin: '', estado: 'PLANIFICADO' };
        this.modalAbierto = true;
    }

    abrirModalEditar(p: PeriodoAcademicoDTO) {
        if (p.estado === 'INACTIVO') return; // Item 6: No editar inactivos
        this.modoEdicion = true;
        this.formulario = { ...p };
        this.modalAbierto = true;
    }

    cerrarModal() {
        this.modalAbierto = false;
    }

    guardarPeriodo() {
        if (this.modoEdicion && this.formulario.idPeriodoAcademico) {
            this.periodoService.actualizar(this.formulario.idPeriodoAcademico, this.formulario as any).subscribe({
                next: () => {
                    this.cargarPeriodos();
                    this.cerrarModal();
                }
            });
        } else {
            this.periodoService.crear(this.formulario as any).subscribe({
                next: () => {
                    this.cargarPeriodos();
                    this.cerrarModal();
                }
            });
        }
    }

    cambiarEstado(p: PeriodoAcademicoDTO, activando: boolean) {
        if (activando) {
            this.periodoService.activar(p.idPeriodoAcademico!).subscribe({
                next: () => this.cargarPeriodos()
            });
        } else {
            this.periodoService.desactivar(p.idPeriodoAcademico!).subscribe({
                next: () => this.cargarPeriodos()
            });
        }
    }

    // --- ITEM 5: Importar requisitos ---
    abrirModalImportar(idDestino: number) {
        this.importarConfig.idDestino = idDestino;
        this.importarConfig.idFuente = null;
        this.modalImportarAbierto = true;
    }

    cerrarModalImportar() {
        this.modalImportarAbierto = false;
    }

    confirmarImportacion() {
        if (!this.importarConfig.idDestino || !this.importarConfig.idFuente) return;
        this.periodoService.importarRequisitos(this.importarConfig.idDestino, this.importarConfig.idFuente).subscribe({
            next: (res: any) => {
                alert(res.mensaje || 'Importado correctamente');
                this.cerrarModalImportar();
            },
            error: (e) => alert('Error: ' + e.error?.error)
        });
    }
}
