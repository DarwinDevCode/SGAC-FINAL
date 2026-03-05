import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DocenteService } from '../../../core/services/docente-service';
import { DocenteDashboardDTO, AyudanteResumenDTO } from '../../../core/dto/docente';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, Users, ClipboardCheck, Clock, XCircle, Eye, TrendingUp, ChevronRight } from 'lucide-angular';

@Component({
    selector: 'app-docente-dashboard',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    providers: [
        { provide: LUCIDE_ICONS, multi: true, useValue: new LucideIconProvider({ Users, ClipboardCheck, Clock, XCircle, Eye, TrendingUp, ChevronRight }) }
    ],
    templateUrl: './dashboard.html',
    styleUrl: './dashboard.css'
})
export class DocenteDashboardComponent implements OnInit {
    private docenteService = inject(DocenteService);
    private router = inject(Router);

    dashboard: DocenteDashboardDTO | null = null;
    ayudantes: AyudanteResumenDTO[] = [];
    loading = true;

    ngOnInit(): void {
        this.docenteService.getDashboard().subscribe({
            next: (d) => { this.dashboard = d; this.loading = false; },
            error: () => { this.loading = false; }
        });
        this.docenteService.getAyudantes().subscribe({
            next: (a) => this.ayudantes = a.slice(0, 5),
            error: () => { }
        });
    }

    verAyudante(idAyudantia: number): void {
        this.router.navigate(['/docente/mis-ayudantes', idAyudantia, 'actividades']);
    }

    irAMisAyudantes(): void {
        this.router.navigate(['/docente/mis-ayudantes']);
    }

    getPct(val: number): number {
        if (!this.dashboard || this.dashboard.totalActividades === 0) return 0;
        return Math.round((val / this.dashboard.totalActividades) * 100);
    }
}
