import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    LucideAngularModule,
    LUCIDE_ICONS,
    LucideIconProvider,
    Users,
    UserCheck,
    Shield,
    CalendarClock,
    Activity,
    HardDrive,
    ChevronRight,
    Inbox,
    TrendingUp,
    BarChart3,
    Loader2,
    CheckCircle,
    AlertCircle,
    FileText,
    Clock,
    Download
} from 'lucide-angular';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartData, ChartOptions, registerables } from 'chart.js';
import { AdminConsultaService, AdminConsultaDTO } from '../../../core/services/admin-consulta-service';

Chart.register(...registerables);

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [CommonModule, LucideAngularModule, RouterLink, BaseChartDirective],
    templateUrl: './dashboard.html',
    styleUrl: './dashboard.css',
    providers: [
        {
            provide: LUCIDE_ICONS,
            multi: true,
            useValue: new LucideIconProvider({
                Users, UserCheck, Shield, CalendarClock, Activity,
                HardDrive, ChevronRight, Inbox, TrendingUp, BarChart3,
                Loader2, CheckCircle, AlertCircle, FileText, Clock, Download
            })
        }
    ]
})
export class DashboardComponent implements OnInit, OnDestroy {
    private dashboardService = inject(AdminConsultaService);

    loading = true;
    error = '';
    data: AdminConsultaDTO | null = null;

    // Line Chart – Tendencia mensual
    lineChartData: ChartData<'line'> = { labels: [], datasets: [] };
    lineChartOptions: ChartOptions<'line'> = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
            legend: { position: 'top', labels: { usePointStyle: true, boxWidth: 8 } },
            tooltip: { enabled: true }
        },
        scales: {
            y: {
                beginAtZero: true,
                grid: { color: '#f1f5f9' },
                ticks: { stepSize: 1 }
            },
            x: { grid: { display: false } }
        }
    };

    // Doughnut Chart – Distribución por roles
    doughnutChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
    doughnutChartOptions: ChartOptions<'doughnut'> = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'right', labels: { usePointStyle: true, boxWidth: 10 } }
        },
        cutout: '65%'
    };

    // Bar Chart – Postulaciones por facultad
    barChartData: ChartData<'bar'> = { labels: [], datasets: [] };
    barChartOptions: ChartOptions<'bar'> = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false }
        },
        scales: {
            y: { beginAtZero: true, grid: { color: '#f1f5f9' } },
            x: { grid: { display: false } }
        }
    };

    ngOnInit() {
        this.dashboardService.obtenerEstadisticas().subscribe({
            next: (dto) => {
                this.data = dto;
                this.buildCharts(dto);
                this.loading = false;
            },
            error: (err) => {
                console.error(err);
                this.error = 'No se pudieron cargar las estadísticas del sistema.';
                this.loading = false;
            }
        });
    }

    ngOnDestroy() { }

    private buildCharts(dto: AdminConsultaDTO) {
        const ROLE_COLORS: Record<string, string> = {
            'ESTUDIANTE': '#3b82f6', 'DOCENTE': '#10b981', 'COORDINADOR': '#8b5cf6',
            'DECANO': '#f59e0b', 'ADMINISTRADOR': '#ef4444', 'AYUDANTE_CATEDRA': '#06b6d4'
        };

        // Line Chart
        const labels = dto.estadisticasMensuales.map((e: any) => e.mes);
        this.lineChartData = {
            labels,
            datasets: [
                {
                    label: 'Postulaciones',
                    data: dto.estadisticasMensuales.map((e: any) => e.postulaciones),
                    borderColor: '#3b82f6',
                    backgroundColor: 'rgba(59,130,246,0.1)',
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#3b82f6',
                    pointRadius: 4
                },
                {
                    label: 'Convocatorias',
                    data: dto.estadisticasMensuales.map((e: any) => e.convocatorias),
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16,185,129,0.1)',
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#10b981',
                    pointRadius: 4
                }
            ]
        };

        // Doughnut Chart
        this.doughnutChartData = {
            labels: dto.distribucionRoles.map((r: any) => r.rol),
            datasets: [{
                data: dto.distribucionRoles.map((r: any) => r.cantidad),
                backgroundColor: dto.distribucionRoles.map((r: any) => ROLE_COLORS[r.rol] || '#94a3b8'),
                borderWidth: 2,
                borderColor: '#fff'
            }]
        };

        // Bar Chart
        this.barChartData = {
            labels: dto.distribucionFacultades.map((f: any) => f.facultad),
            datasets: [{
                label: 'Postulaciones',
                data: dto.distribucionFacultades.map((f: any) => f.cantidadPostulaciones),
                backgroundColor: '#6366f1',
                borderRadius: 6,
                hoverBackgroundColor: '#4f46e5'
            }]
        };
    }

    getTimeAgo(fechaStr: string): string {
        if (!fechaStr) return '';
        const diff = Date.now() - new Date(fechaStr).getTime();
        const mins = Math.floor(diff / 60000);
        if (mins < 60) return `Hace ${mins}m`;
        const hrs = Math.floor(mins / 60);
        if (hrs < 24) return `Hace ${hrs}h`;
        return `Hace ${Math.floor(hrs / 24)}d`;
    }
}
