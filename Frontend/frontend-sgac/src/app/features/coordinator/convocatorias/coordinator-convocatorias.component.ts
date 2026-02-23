import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { FiltersBarComponent } from '../../../shared/components/filters-bar/filters-bar.component';
import { LucideAngularModule, Plus, Users, Settings, FileText } from 'lucide-angular';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';

@Component({
    selector: 'app-coordinator-convocatorias',
    standalone: true,
    imports: [CommonModule, RouterModule, StatusChipComponent, FiltersBarComponent, LucideAngularModule],
    templateUrl: './coordinator-convocatorias.component.html'
})
export class CoordinatorConvocatoriasComponent implements OnInit {
    private convocatoriasService = inject(ConvocatoriaService);
    private router = inject(Router);

    convocatorias = signal<ConvocatoriaDTO[]>([]);
    loading = signal(true);
    searchQuery = signal('');

    readonly Plus = Plus;
    readonly Users = Users;
    readonly Settings = Settings;
    readonly FileText = FileText;

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

    manage(id: number) {
        this.router.navigate(['/coordinator/convocatorias', id]);
    }
}
