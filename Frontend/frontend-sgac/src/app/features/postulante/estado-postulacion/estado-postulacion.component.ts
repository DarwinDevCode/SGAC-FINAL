import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { PostulanteService } from '../../../core/services/postulante-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';

@Component({
    selector: 'app-estado-postulacion',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
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
                    console.error('Error al cargar mis postulaciones:', err);
                    this.loading = false;
                }
            })
        );
    }
}
