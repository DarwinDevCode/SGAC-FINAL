import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { HttpClient } from '@angular/common/http';
import { Subscription, switchMap } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';

/**
 * P13 (Ítem 15): Vista de Evaluación y Ranking del coordinador.
 * Selecciona una convocatoria, carga el ranking desde el SP,
 * permite ingresar puntajes de méritos y oposición.
 */
@Component({
    selector: 'app-coordinador-evaluaciones',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    templateUrl: './evaluaciones.html',
    styleUrl: './evaluaciones.css',
})
export class EvaluacionesComponent implements OnInit, OnDestroy {
    private coordinadorService = inject(CoordinadorService);
    private authService = inject(AuthService);
    private http = inject(HttpClient);
    private subs = new Subscription();

    private readonly BASE = 'http://localhost:8080/api';

    convocatorias: any[] = [];
    ranking: any[] = [];
    idConvSeleccionada = 0;
    loadingConv = true;
    loadingRanking = false;

    // Formulario de puntajes
    puntajesMeritos: Record<number, number | null> = {};
    puntajesOposicion: Record<number, number | null> = {};
    guardandoPuntaje: Record<number, boolean> = {};

    successMsg = '';
    errorMsg = '';

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (!user) { this.loadingConv = false; return; }

        this.subs.add(
            this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).pipe(
                switchMap((coord: any) =>
                    this.coordinadorService.listarConvocatoriasPorCarrera(coord.idCarrera)
                )
            ).subscribe({
                next: (convs: any[]) => {
                    this.convocatorias = convs;
                    this.loadingConv = false;
                },
                error: () => { this.loadingConv = false; }
            })
        );
    }

    ngOnDestroy(): void { this.subs.unsubscribe(); }

    cargarRanking() {
        if (!this.idConvSeleccionada) return;
        this.loadingRanking = true;
        this.ranking = [];
        this.subs.add(
            this.http.get<any[]>(`${this.BASE}/evaluaciones/ranking/convocatoria/${this.idConvSeleccionada}`).subscribe({
                next: (data) => { this.ranking = data || []; this.loadingRanking = false; },
                error: () => { this.loadingRanking = false; }
            })
        );
    }

    guardarMeritos(idPostulacion: number) {
        const puntaje = this.puntajesMeritos[idPostulacion];
        if (puntaje == null) return;
        this.guardandoPuntaje[idPostulacion] = true;
        this.subs.add(
            this.http.post(`${this.BASE}/evaluaciones/meritos`, { idPostulacion, puntajeTotal: puntaje }).subscribe({
                next: () => {
                    this.guardandoPuntaje[idPostulacion] = false;
                    this.showSuccess('Méritos guardados correctamente.');
                    this.cargarRanking();
                },
                error: () => { this.guardandoPuntaje[idPostulacion] = false; this.showError('Error al guardar méritos.'); }
            })
        );
    }

    guardarOposicion(idPostulacion: number) {
        const puntaje = this.puntajesOposicion[idPostulacion];
        if (puntaje == null) return;
        this.guardandoPuntaje[idPostulacion] = true;
        this.subs.add(
            this.http.post(`${this.BASE}/evaluaciones/oposicion`, { idPostulacion, puntajeTotal: puntaje }).subscribe({
                next: () => {
                    this.guardandoPuntaje[idPostulacion] = false;
                    this.showSuccess('Oposición guardada correctamente.');
                    this.cargarRanking();
                },
                error: () => { this.guardandoPuntaje[idPostulacion] = false; this.showError('Error al guardar oposición.'); }
            })
        );
    }

    private showSuccess(msg: string) {
        this.successMsg = msg;
        setTimeout(() => this.successMsg = '', 4000);
    }

    private showError(msg: string) {
        this.errorMsg = msg;
        setTimeout(() => this.errorMsg = '', 4000);
    }
}
