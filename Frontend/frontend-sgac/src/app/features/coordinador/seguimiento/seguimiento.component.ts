import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth-service';
import { InformeMensualService } from '../../../core/services/informe-mensual.service';
import { InformeMensualResponse } from '../../../core/dto/informe-mensual-response';

@Component({
    selector: 'app-coordinador-seguimiento',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    templateUrl: './seguimiento.html',
    styleUrl: './seguimiento.css',
})
export class SeguimientoComponent implements OnInit, OnDestroy {
    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private informeService = inject(InformeMensualService);
    private subs = new Subscription();

    ayudantes: any[] = [];
    informes: InformeMensualResponse[] = [];
    loading = true;
    errorMensaje = '';
    activeTab: 'ayudantes' | 'informes' = 'informes';

    // Modal revisio
    modalVisible = false;
    modalEstado = '';
    modalObservacion = '';
    modalIdReferencia = 0;
    guardando = false;

    ngOnInit(): void { this.cargarDatos(); }
    ngOnDestroy(): void { this.subs.unsubscribe(); }

    cargarDatos() {
        this.loading = true;
        this.cargarAyudantes();
        this.cargarInformes();
    }

    cargarAyudantes() {
        this.subs.add(
            this.http.get<any[]>('http://localhost:8080/api/ayudantes/listar').subscribe({
                next: (data) => { this.ayudantes = data || []; this.loading = false; },
                error: () => { this.loading = false; }
            })
        );
    }

    cargarInformes() {
        this.subs.add(
            this.informeService.listarPendientesCoordinador().subscribe({
                next: (data) => { this.informes = data || []; this.loading = false; },
                error: () => { this.errorMensaje = 'No se pudieron cargar los informes pendientes.'; this.loading = false; }
            })
        );
    }

    iniciarRevisarInforme(idInformeMensual: number): void {
        this.modalIdReferencia = idInformeMensual;
        this.modalEstado = 'ACEPTADO';
        this.modalObservacion = '';
        this.modalVisible = true;
    }

    confirmarCambio(): void {
        if (this.modalEstado === 'OBSERVADO' && !this.modalObservacion.trim()) {
            alert('Debes escribir una observación');
            return;
        }

        this.guardando = true;
        let obs$: any;
        if (this.modalEstado === 'ACEPTADO') {
            obs$ = this.informeService.aprobarInforme(this.modalIdReferencia, 'COORDINADOR');
        } else if (this.modalEstado === 'OBSERVADO') {
            obs$ = this.informeService.observarInforme(this.modalIdReferencia, this.modalObservacion);
        } else {
            obs$ = this.informeService.rechazarInforme(this.modalIdReferencia, this.modalObservacion);
        }

        this.subs.add(
            obs$.subscribe({
                next: () => {
                    this.modalVisible = false;
                    this.guardando = false;
                    this.cargarDatos();
                },
                error: (err: any) => { this.guardando = false; alert(err.error?.message || 'Error al guardar el estado del informe'); }
            })
        );
    }

    cerrarModal(): void { this.modalVisible = false; }
}
