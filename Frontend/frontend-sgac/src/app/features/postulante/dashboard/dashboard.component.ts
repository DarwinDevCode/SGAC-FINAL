import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';

@Component({
    selector: 'app-postulante-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './dashboard.html',
    styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
    postulanteService = inject(PostulanteService);
    authService = inject(AuthService);
    private subs = new Subscription();

    totalConvocatorias = 0;
    totalPostulaciones = 0;
    loading = true;
    idEstudianteBase = 0;

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (user) {
            this.idEstudianteBase = user.idUsuario;
        }
        this.cargarDatosDashboard();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarDatosDashboard() {
        this.loading = true;

        // Cargar número de convocatorias activas
        this.subs.add(
            this.postulanteService.listarConvocatoriasActivas().subscribe({
                next: (convocatorias) => {
                    this.totalConvocatorias = convocatorias?.length || 0;
                    this.checkLoading();
                },
                error: () => this.checkLoading()
            })
        );

        // Cargar número de postulaciones activas del estudiante
        this.subs.add(
            this.postulanteService.misPostulaciones(this.idEstudianteBase).subscribe({
                next: (postulaciones) => {
                    this.totalPostulaciones = postulaciones?.length || 0;
                    this.checkLoading();
                },
                error: () => this.checkLoading()
            })
        );
    }

    // Pequeño hack para quitar el loading solo cuando ambas peticiones terminen (podría usarse forkJoin)
    private requestsCompleted = 0;
    private checkLoading() {
        this.requestsCompleted++;
        if (this.requestsCompleted >= 2) {
            this.loading = false;
        }
    }
}
