import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { ConvocatoriaDTO } from '../../../core/dto/convocatoria';

@Component({
    selector: 'app-decano-convocatorias',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './convocatorias-vista.html',
    styleUrl: './convocatorias-vista.css',
})
export class ConvocatoriasVistaComponent implements OnInit, OnDestroy {
    decanoService = inject(DecanoService);
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
            this.errorMensaje = 'No se pudo verificar la sesión actual.';
            this.loading = false;
            return;
        }

        this.subs.add(
            this.decanoService.obtenerDecanoPorUsuario(user.idUsuario).subscribe({
                next: (decano) => {
                    this.subs.add(
                        this.decanoService.listarConvocatoriasActivas().subscribe({
                            next: (lista) => {
                                this.convocatorias = lista;
                                this.loading = false;
                            },
                            error: () => this.loading = false
                        })
                    );
                },
                error: () => {
                    this.errorMensaje = 'No tienes permisos de decano registrados.';
                    this.loading = false;
                }
            })
        );
    }
}
