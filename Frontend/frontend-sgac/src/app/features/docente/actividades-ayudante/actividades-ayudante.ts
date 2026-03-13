import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DocenteService } from '../../../core/services/docente-service';
import { InformeMensualService } from '../../../core/services/informe-mensual.service';
import { InformeMensualResponse } from '../../../core/dto/informe-mensual-response';
import { RegistroActividadDocenteDTO, EvidenciaDocenteDTO, CambiarEstadoRequest } from '../../../core/dto/docente';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, ChevronDown, ChevronUp, CheckCircle, XCircle, Clock, Eye, ArrowLeft, Send, FileText } from 'lucide-angular';

@Component({
    selector: 'app-actividades-ayudante',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    providers: [
        { provide: LUCIDE_ICONS, multi: true, useValue: new LucideIconProvider({ ChevronDown, ChevronUp, CheckCircle, XCircle, Clock, Eye, ArrowLeft, Send, FileText }) }
    ],
    templateUrl: './actividades-ayudante.html',
    styleUrl: './actividades-ayudante.css'
})
export class ActividadesAyudanteComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private docenteService = inject(DocenteService);
    private informeService = inject(InformeMensualService);

    idAyudantia!: number;
    actividades: RegistroActividadDocenteDTO[] = [];
    informes: InformeMensualResponse[] = [];
    loading = true;
    activeTab: 'actividades' | 'informes' = 'actividades';

    // Modal observacion
    modalVisible = false;
    modalTipo: 'actividad' | 'evidencia' | 'informe' = 'actividad';
    modalEstado = '';
    modalObservacion = '';
    modalIdReferencia = 0;
    guardando = false;

    readonly ESTADOS = ['PENDIENTE', 'ACEPTADO', 'RECHAZADO', 'OBSERVADO'];

    ngOnInit(): void {
        this.idAyudantia = Number(this.route.snapshot.paramMap.get('idAyudantia'));
        this.cargarActividades();
    }

    cargarActividades(): void {
        this.loading = true;
        this.docenteService.getActividadesAyudante(this.idAyudantia).subscribe({
            next: (a) => { this.actividades = a; this.cargarInformes(); },
            error: () => { this.loading = false; }
        });
    }

    cargarInformes(): void {
        this.informeService.listarPorAyudantia(this.idAyudantia).subscribe({
            next: (inf) => { this.informes = inf; this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    toggleActividad(act: RegistroActividadDocenteDTO): void {
        act.expandido = !act.expandido;
    }

    // ── Cambio estado actividad global ────────────────────────────────────────

    iniciarCambioActividad(idActividad: number, estadoActual: string): void {
        this.modalTipo = 'actividad';
        this.modalIdReferencia = idActividad;
        this.modalEstado = estadoActual;
        this.modalObservacion = '';
        this.modalVisible = true;
    }

    // ── Cambio estado evidencia ───────────────────────────────────────────────

    iniciarCambioEvidencia(idEvidencia: number, estadoActual: string): void {
        this.modalTipo = 'evidencia';
        this.modalIdReferencia = idEvidencia;
        this.modalEstado = estadoActual;
        this.modalObservacion = '';
        this.modalVisible = true;
    }

    confirmarCambio(): void {
        if (this.modalEstado === 'OBSERVADO' && !this.modalObservacion.trim()) {
            alert('Debes escribir una observación');
            return;
        }

        if (this.modalTipo === 'informe') {
            this.guardando = true;
            let obs$: any;
            if (this.modalEstado === 'ACEPTADO') {
                obs$ = this.informeService.aprobarInforme(this.modalIdReferencia, 'DOCENTE');
            } else if (this.modalEstado === 'OBSERVADO') {
                obs$ = this.informeService.observarInforme(this.modalIdReferencia, this.modalObservacion);
            } else {
                obs$ = this.informeService.rechazarInforme(this.modalIdReferencia, this.modalObservacion);
            }

            obs$.subscribe({
                next: () => {
                    this.modalVisible = false;
                    this.guardando = false;
                    this.cargarInformes();
                },
                error: (err: any) => { this.guardando = false; alert(err.error?.message || 'Error al guardar el estado del informe'); }
            });
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

    iniciarRevisarInforme(idInformeMensual: number, estadoActual: string): void {
        this.modalTipo = 'informe' as any;
        this.modalIdReferencia = idInformeMensual;
        this.modalEstado = estadoActual === 'EN_REVISION_DOCENTE' ? 'ACEPTADO' : estadoActual;
        this.modalObservacion = '';
        this.modalVisible = true;
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
