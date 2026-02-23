import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { StatusChipComponent } from '../../../../shared/components/status-chip/status-chip.component';
import { LucideAngularModule, Users, UserPlus, Trash2, CheckCircle, FileText, ArrowLeft, Send } from 'lucide-angular';
import {ConvocatoriaService} from '../../../../core/services/convocatoria-service';
import {ConvocatoriaDTO} from '../../../../core/dto/convocatoria';

@Component({
    selector: 'app-coordinator-convocatoria-detalle',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, StatusChipComponent, LucideAngularModule],
    templateUrl: './coordinator-convocatoria-detalle.component.html'
})
export class CoordinatorConvocatoriaDetalleComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private convocatoriasService = inject(ConvocatoriaService);

    convocatoria = signal<ConvocatoriaDTO | null>(null);
    comision = signal<any[]>([]);
    loading = signal(true);

    showAddMemberModal = false;
    availableDocentes = signal<any[]>([]);
    selectedDocenteId = 0;
    selectedRole = 'MIEMBRO';

    readonly Users = Users;
    readonly UserPlus = UserPlus;
    readonly Trash2 = Trash2;
    readonly CheckCircle = CheckCircle;
    readonly FileText = FileText;
    readonly ArrowLeft = ArrowLeft;
    readonly Send = Send;

    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadData(+id);
        }
    }

    loadData(id: number) {
        this.loading.set(true);
        this.convocatoriasService.getById(id).subscribe({
            next: (data) => {
                this.convocatoria.set(data);
                this.loadCommission(id);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    loadCommission(id: number) {
        this.comision.set([
            { idUsuario: 101, nombre: 'Dr. Roberto Gómez', rol: 'PRESIDENTE' },
            { idUsuario: 102, nombre: 'MSc. Ana Torres', rol: 'MIEMBRO' }
        ]);
    }

    openAddMemberModal() {
        this.availableDocentes.set([
            { id: 201, nombre: 'Ing. Carlos Ruiz' },
            { id: 202, nombre: 'Lic. Maria Lujan' },
            { id: 203, nombre: 'Dr. Jose Perez' }
        ]);
        this.showAddMemberModal = true;
    }

    addMember() {
        if (!this.selectedDocenteId) return;
        const doc = this.availableDocentes().find(d => d.id == this.selectedDocenteId);

        console.log('Adding member:', doc, this.selectedRole);
        this.comision.update(current => [...current, { idUsuario: doc.id, nombre: doc.nombre, rol: this.selectedRole }]);
        this.showAddMemberModal = false;
    }

    removeMember(userId: number) {
        if (confirm('¿Eliminar miembro de la comisión?')) {
            this.comision.update(current => current.filter(m => m.idUsuario !== userId));
        }
    }

    approveResults() {
        if (confirm('¿Está seguro de consolidar los resultados y enviarlos al Decano? Esta acción no se puede deshacer.')) {
            alert('Resultados consolidados y enviados exitosamente.');
            this.convocatoria.update(c => c ? { ...c, estado: 'LISTA_PARA_APROBAR' } : null);
        }
    }
}
