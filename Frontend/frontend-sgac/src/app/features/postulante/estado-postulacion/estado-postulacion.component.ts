import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';

@Component({
    selector: 'app-estado-postulacion',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './estado-postulacion.html',
    styleUrl: './estado-postulacion.css',
})
export class EstadoPostulacionComponent implements OnInit, OnDestroy {
    postulanteService = inject(PostulanteService);
    authService = inject(AuthService);
    private subs = new Subscription();

    postulaciones: PostulacionResponseDTO[] = [];
    loading = true;
    idEstudianteBase = 0;

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (user) {
            this.idEstudianteBase = user.idUsuario;
        }
        this.cargarMisPostulaciones();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarMisPostulaciones() {
        this.loading = true;
        this.subs.add(
            this.postulanteService.misPostulaciones(this.idEstudianteBase).subscribe({
                next: (data) => {
                    this.postulaciones = data || [];
                    this.loading = false;
                },
                error: (err) => {
                    console.error(err.error?.mensaje || err.message || 'Error al cargar postulaciones');
                    this.loading = false;
                }
            })
        );
    }

    getPhase(estado: string | undefined): number {
        if (!estado) return 1;
        const e = estado.toUpperCase();
        if (e === 'PENDIENTE') return 1; // Revisión inicial
        if (e === 'EN_EVALUACION' || e === 'MERITOS_EVALUADOS') return 2; // Méritos
        if (e === 'OPOSICION_EVALUADA') return 3; // Oposición
        if (e === 'APROBADO' || e === 'RECHAZADO') return 4; // Resultados
        return 1;
    }
}
