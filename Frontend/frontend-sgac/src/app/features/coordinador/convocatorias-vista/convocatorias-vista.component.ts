import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';
import { ConvocatoriaDTO } from '../../../core/dto/convocatoria';

@Component({
    selector: 'app-coordinador-convocatorias',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './convocatorias-vista.html',
    styleUrl: './convocatorias-vista.css',
})
export class ConvocatoriasVistaComponent implements OnInit, OnDestroy {
    coordinadorService = inject(CoordinadorService);
    authService = inject(AuthService);
    private subs = new Subscription();

    convocatorias: ConvocatoriaDTO[] = [];
    loading = true;
    errorMensaje = '';

    ngOnInit(): void {
        this.cargarConvocatorias();
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarConvocatorias() {
        this.loading = true;
        const user = this.authService.getUser();

        if (!user) {
            this.errorMensaje = 'No se pudo verificar la sesión.';
            this.loading = false;
            return;
        }

        this.subs.add(
            this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).subscribe({
                next: (coord) => {
                    this.subs.add(
                        this.coordinadorService.listarConvocatoriasPorCarrera(coord.idCarrera).subscribe({
                            next: (lista) => {
                                // If the backend returns all, we might need to filter manually.
                                // However, without a carrera field in ConvocatoriaDTO, we'll show the list
                                // that the service returns.
                                this.convocatorias = lista;
                                this.loading = false;
                            },
                            error: () => this.loading = false
                        })
                    );
                },
                error: () => {
                    this.errorMensaje = 'Error al cargar perfil de coordinador.';
                    this.loading = false;
                }
            })
        );
    }
}
