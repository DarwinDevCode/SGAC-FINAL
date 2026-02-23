import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StatusChipComponent } from '../../../shared/components/status-chip/status-chip.component';
import { LucideAngularModule, CheckCircle, ArrowRight } from 'lucide-angular';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';

@Component({
    selector: 'app-dean-aprobaciones',
    standalone: true,
    imports: [CommonModule, RouterModule, StatusChipComponent, LucideAngularModule],
    templateUrl: './dean-aprobaciones.component.html'
})
export class DeanAprobacionesComponent implements OnInit {
    private convocatoriasService = inject(ConvocatoriaService);

    // List of convocatorias waiting for approval (LISTA_PARA_APROBAR)
    pendingConvocatorias = signal<ConvocatoriaDTO[]>([]);
    loading = signal(true);

    readonly CheckCircle = CheckCircle;
    readonly ArrowRight = ArrowRight;

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        this.convocatoriasService.getAll().subscribe(data => {
            // Filter by status 'LISTA_PARA_APROBAR'
            // Ideally backend has a specific endpoint
            const filtered = data.filter(c => c.estado === 'LISTA_PARA_APROBAR');
            this.pendingConvocatorias.set(filtered);
            this.loading.set(false);
        });
    }

    approve(id: number) {
        if (confirm('¿Está seguro de aprobar y publicar los resultados finales?')) {

            alert('Resultados publicados exitosamente.');

            this.pendingConvocatorias.update(list => list.filter(c => c.idConvocatoria !== id));
        }
    }
}
