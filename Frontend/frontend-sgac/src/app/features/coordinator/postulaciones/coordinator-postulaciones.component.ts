import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { FiltersBarComponent } from '../../../shared/components/filters-bar/filters-bar.component';
import { LucideAngularModule, Eye, Filter } from 'lucide-angular';
import {PostulacionService} from '../../../core/services/postulacion-service';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';
import {PostulacionDTO} from '../../../core/dto/postulacion';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';

@Component({
    selector: 'app-coordinator-postulaciones',
    standalone: true,
    imports: [CommonModule, FormsModule, StatusChipComponent, FiltersBarComponent, LucideAngularModule],
    templateUrl: './coordinator-postulaciones.component.html'
})
export class CoordinatorPostulacionesComponent implements OnInit {
    private postulacionService = inject(PostulacionService);
    private convocatoriasService = inject(ConvocatoriaService);
    private router = inject(Router);

    postulaciones = signal<PostulacionDTO[]>([]);
    convocatorias = signal<ConvocatoriaDTO[]>([]);
    loading = signal(false);

    searchQuery = signal('');
    selectedConvocatoria = signal<number | 'all'>('all');
    selectedEstado = signal<string>('all');

    readonly Eye = Eye;
    readonly Filter = Filter;

    filteredPostulaciones = computed(() => {
        return this.postulaciones().filter(p => {
            const matchSearch = (p.idEstudiante?.toString().includes(this.searchQuery()) || false); // Improve if we have student name
            const matchConv = this.selectedConvocatoria() === 'all' || p.idConvocatoria === Number(this.selectedConvocatoria());
            const matchEstado = this.selectedEstado() === 'all' || p.estadoPostulacion === this.selectedEstado();
            return matchSearch && matchConv && matchEstado;
        });
    });

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        // Load Convocatorias for filter
        this.convocatoriasService.getAll().subscribe(data => this.convocatorias.set(data));

        // Load All Postulaciones (We might need a new endpoint for "all" or iterate convocatorias)
        // For now, let's assume we can get all or by convocatoria.
        // The user said "Bandeja por convocatoria GET /api/postulaciones/convocatoria/{id}".
        // UX: Usually clear to select a convocatoria first or show all.
        // Let's load the first open convocatoria by default or just empty list until filter selected.
        // Or if we have "getAll" for admin?
        // Let's try to load all by iterating open convocatorias or just wait for user selection.
        // Better UX: Load all open convocatorias, then maybe auto-select first one.
        this.loading.set(false);
    }

    onConvocatoriaChange() {
        if (this.selectedConvocatoria() !== 'all') {
            this.loading.set(true);
            this.postulacionService.listarPorConvocatoria(Number(this.selectedConvocatoria())).subscribe({
                next: (data) => {
                    this.postulaciones.set(data);
                    this.loading.set(false);
                },
                error: () => this.loading.set(false)
            });
        } else {
            this.postulaciones.set([]);
        }
    }

    onReview(id: number) {
        this.router.navigate(['/coordinator/postulaciones/revisar', id]);
    }
}
