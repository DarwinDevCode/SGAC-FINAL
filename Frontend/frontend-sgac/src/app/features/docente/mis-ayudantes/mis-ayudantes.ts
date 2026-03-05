import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DocenteService } from '../../../core/services/docente-service';
import { AyudanteResumenDTO } from '../../../core/dto/docente';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, User, BookOpen, Clock, ChevronRight, Search } from 'lucide-angular';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-mis-ayudantes',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    providers: [
        { provide: LUCIDE_ICONS, multi: true, useValue: new LucideIconProvider({ User, BookOpen, Clock, ChevronRight, Search }) }
    ],
    templateUrl: './mis-ayudantes.html',
    styleUrl: './mis-ayudantes.css'
})
export class MisAyudantesComponent implements OnInit {
    private docenteService = inject(DocenteService);
    private router = inject(Router);

    ayudantes: AyudanteResumenDTO[] = [];
    filtrados: AyudanteResumenDTO[] = [];
    busqueda = '';
    loading = true;

    ngOnInit(): void {
        this.docenteService.getAyudantes().subscribe({
            next: (a) => { this.ayudantes = a; this.filtrados = a; this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    filtrar(): void {
        const q = this.busqueda.toLowerCase();
        this.filtrados = this.ayudantes.filter(a =>
            a.nombreCompleto.toLowerCase().includes(q) ||
            a.nombreAsignatura.toLowerCase().includes(q)
        );
    }

    verActividades(idAyudantia: number): void {
        this.router.navigate(['/docente/mis-ayudantes', idAyudantia, 'actividades']);
    }

    getEstadoBadge(estado: string): string {
        switch (estado?.toUpperCase()) {
            case 'ACTIVO': return 'badge-activo';
            case 'FINALIZADO': return 'badge-finalizado';
            case 'SUSPENDIDO': return 'badge-suspendido';
            default: return 'badge-default';
        }
    }
}
