import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-coordinador-validaciones',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './validaciones.html',
    styleUrl: './validaciones.css',
})
export class ValidacionesComponent implements OnInit, OnDestroy {
    private coordinadorService = inject(CoordinadorService);
    private authService = inject(AuthService);
    private http = inject(HttpClient);
    private subs = new Subscription();

    postulantes: PostulacionResponseDTO[] = [];
    loading = true;
    errorMensaje = '';
    successMensaje = '';

    ngOnInit(): void {
        this.cargarPostulantes();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarPostulantes() {
        this.loading = true;
        const user = this.authService.getUser();
        if (!user) { this.loading = false; return; }

        this.subs.add(
            this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).subscribe({
                next: (coord) => {
                    this.subs.add(
                        this.coordinadorService.listarPostulacionesPorConvocatoria(0).subscribe({
                            next: (lista) => {
                                this.postulantes = lista.filter(p => p.estadoPostulacion === 'PENDIENTE');
                                this.loading = false;
                            },
                            error: () => this.loading = false
                        })
                    );
                },
                error: () => {
                    // Cargar todas las postulaciones pendientes sin filtrar por carrera si no hay coord
                    this.http.get<PostulacionResponseDTO[]>('http://localhost:8080/api/postulaciones/listar').subscribe({
                        next: (lista) => {
                            this.postulantes = lista.filter(p => p.estadoPostulacion === 'PENDIENTE');
                            this.loading = false;
                        },
                        error: () => { this.errorMensaje = 'Error al cargar postulaciones.'; this.loading = false; }
                    });
                }
            })
        );
    }
}
