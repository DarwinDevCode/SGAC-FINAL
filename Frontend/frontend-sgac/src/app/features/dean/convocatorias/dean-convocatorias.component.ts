import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { FiltersBarComponent } from '../../../shared/components/filters-bar/filters-bar.component';
import { LucideAngularModule, Plus, Edit, Trash2, Eye } from 'lucide-angular';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';

@Component({
    selector: 'app-dean-convocatorias',
    standalone: true,
    imports: [CommonModule, RouterModule, StatusChipComponent, FiltersBarComponent, LucideAngularModule],
    templateUrl: './dean-convocatorias.component.html'
})
export class DeanConvocatoriasComponent implements OnInit {
    private convocatoriasService = inject(ConvocatoriaService);
    private router = inject(Router);

    convocatorias = signal<ConvocatoriaDTO[]>([]);
    loading = signal(true);
    searchQuery = signal('');

    // Icons
    readonly Plus = Plus;
    readonly Edit = Edit;
    readonly Trash2 = Trash2;
    readonly Eye = Eye;

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        this.convocatoriasService.getAll().subscribe({
            next: (data) => {
                this.convocatorias.set(data);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    create() {
        this.router.navigate(['/dean/convocatorias/crear']);
    }

    edit(id: number) {
        this.router.navigate(['/dean/convocatorias/editar', id]);
    }

    delete(id: number) {
        if (confirm('¿Está seguro de eliminar esta convocatoria?')) {
            this.convocatoriasService.delete(id).subscribe(() => {
                this.loadData();
            });
        }
    }
}
