import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { DecanoService } from '../../../core/services/decano-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';

@Component({
    selector: 'app-decano-postulantes',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './postulantes-vista.html',
    styleUrl: './postulantes-vista.css',
})
export class PostulantesVistaComponent implements OnInit, OnDestroy {
    decanoService = inject(DecanoService);
    route = inject(ActivatedRoute);
    private subs = new Subscription();

    idConvocatoria: number | null = null;
    postulantes: PostulacionResponseDTO[] = [];
    loading = true;
    errorMensaje = '';

    ngOnInit(): void {
        this.subs.add(
            this.route.paramMap.subscribe(params => {
                const id = params.get('idConvocatoria');
                if (id) {
                    this.idConvocatoria = Number(id);
                    this.cargarPostulantes(this.idConvocatoria);
                } else {
                    this.errorMensaje = 'No se proporcionó un ID de convocatoria válido.';
                    this.loading = false;
                }
            })
        );
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarPostulantes(id: number) {
        this.loading = true;
        this.subs.add(
            this.decanoService.listarPostulacionesPorConvocatoria(id).subscribe({
                next: (lista) => {
                    this.postulantes = lista;
                    this.loading = false;
                },
                error: (err) => {
                    console.error('Error al cargar postulantes:', err);
                    this.errorMensaje = 'Ocurrió un error al cargar la lista de postulantes. Intenta nuevamente.';
                    this.loading = false;
                }
            })
        );
    }
}
