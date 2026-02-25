import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { PostulacionResponseDTO } from '../../../core/dto/postulacion';
import { ConvocatoriaDTO } from '../../../core/dto/convocatoria';

const API_CONV = 'http://localhost:8080/api/convocatorias';

@Component({
    selector: 'app-coordinador-postulantes',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './postulantes-vista.html',
    styleUrl: './postulantes-vista.css',
})
export class PostulantesVistaComponent implements OnInit, OnDestroy {
    coordinadorService = inject(CoordinadorService);
    http = inject(HttpClient);
    route = inject(ActivatedRoute);
    private subs = new Subscription();

    idConvocatoria: number | null = null;
    convocatoria: ConvocatoriaDTO | null = null;
    postulantes: PostulacionResponseDTO[] = [];

    loading = true;
    errorMensaje = '';

    ngOnInit(): void {
        this.subs.add(
            this.route.paramMap.subscribe(params => {
                const id = params.get('idConvocatoria');
                if (id) {
                    this.idConvocatoria = Number(id);
                    this.cargarDatos(this.idConvocatoria);
                } else {
                    this.errorMensaje = 'Convocatoria no especificada.';
                    this.loading = false;
                }
            })
        );
    }

    ngOnDestroy(): void {
        this.subs.unsubscribe();
    }

    cargarDatos(id: number) {
        this.loading = true;
        this.subs.add(
            forkJoin({
                detalles: this.http.get<ConvocatoriaDTO>(`${API_CONV}/${id}`).pipe(catchError(() => of(null))),
                lista: this.coordinadorService.listarPostulacionesPorConvocatoria(id).pipe(catchError(() => of([])))
            }).subscribe({
                next: ({ detalles, lista }) => {
                    this.convocatoria = detalles;
                    this.postulantes = lista as PostulacionResponseDTO[];
                    this.loading = false;
                },
                error: () => {
                    this.errorMensaje = 'Error al cargar los datos de la convocatoria.';
                    this.loading = false;
                }
            })
        );
    }

    // Safe date parser in case backend hasn't been restarted with string dates
    formatFecha(fecha: any): any {
        if (!fecha) return null;
        if (Array.isArray(fecha)) {
            return new Date(fecha[0], (fecha[1] ?? 1) - 1, fecha[2] ?? 1);
        }
        return fecha;
    }
}
