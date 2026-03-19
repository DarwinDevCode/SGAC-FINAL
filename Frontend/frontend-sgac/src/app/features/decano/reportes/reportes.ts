import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { DecanoService } from '../../../core/services/decano-service';
import { AuthService } from '../../../core/services/auth-service';
import { ConvocatoriaReporteDTO, CoordinadorPostulanteReporteDTO, DecanoEstadisticasDTO } from '../../../core/dto/decano';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

type ReportType = 'CONVOCATORIAS_GENERAL' | 'CONVOCATORIAS_ESTADO' | 'POSTULANTES_GENERAL' | 'POSTULANTES_VERSUS' | 'POSTULANTES_DESGLOSE' | 'RESUMEN_EJECUTIVO';

@Component({
    selector: 'app-decano-reportes',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    templateUrl: './reportes.html',
    styleUrl: './reportes.css'
})
export class ReportesComponent implements OnInit {
    private decanoService = inject(DecanoService);
    private authService = inject(AuthService);

    loading = true;
    errorMensaje = '';
    idFacultad?: number;

    activeReport: ReportType = 'CONVOCATORIAS_GENERAL';
    filtroTabla = '';

    convocatorias: ConvocatoriaReporteDTO[] = [];
    postulantes: CoordinadorPostulanteReporteDTO[] = [];
    estadisticas?: DecanoEstadisticasDTO;

    ngOnInit() {
        this.cargarDatos();
    }

    async cargarDatos() {
        try {
            this.loading = true;
            const user = this.authService.getUser();
            if (!user) throw new Error('Usuario no autenticado');

            const decano = await this.decanoService.obtenerDecanoPorUsuario(user.idUsuario).toPromise();
            if (!decano) throw new Error('No se encontró información del decano');

            this.idFacultad = decano.idFacultad;

            // Carga paralela de datos base
            const [convs, posts, stats] = await Promise.all([
                this.decanoService.obtenerReporteConvocatorias(this.idFacultad).toPromise(),
                this.decanoService.obtenerReportePostulantes(this.idFacultad).toPromise(),
                this.decanoService.obtenerEstadisticasPorFacultad(this.idFacultad).toPromise()
            ]);

            this.convocatorias = convs || [];
            this.postulantes = posts || [];
            this.estadisticas = stats;

        } catch (err: any) {
            this.errorMensaje = 'Error al cargar los reportes. Por favor, reintente.';
            console.error(err);
        } finally {
            this.loading = false;
        }
    }

    setReport(type: ReportType) {
        this.activeReport = type;
        this.filtroTabla = '';
    }

    get dataVistaPrevia() {
        let data: any[] = [];
        if (this.activeReport.startsWith('CONVOCATORIAS')) {
            data = this.convocatorias;
            if (this.activeReport === 'CONVOCATORIAS_ESTADO') {
                data = [...this.convocatorias].sort((a, b) => a.estado.localeCompare(b.estado));
            }
        } else if (this.activeReport.startsWith('POSTULANTES')) {
            data = this.postulantes;
            if (this.activeReport === 'POSTULANTES_VERSUS') {
                data = this.postulantes.filter(p => p.estadoEvaluacion === 'SELECCIONADO' || p.estadoEvaluacion === 'NO_SELECCIONADO');
            }
            if (this.activeReport === 'POSTULANTES_DESGLOSE') {
                data = [...this.postulantes].sort((a, b) => a.nombreAsignatura.localeCompare(b.nombreAsignatura));
            }
        }

        if (!this.filtroTabla) return data.slice(0, 15);

        const busqueda = this.filtroTabla.toLowerCase();
        return data.filter(item =>
            Object.values(item).some(val => String(val).toLowerCase().includes(busqueda))
        ).slice(0, 15);
    }

    exportar(formato: 'PDF' | 'EXCEL') {
        if (formato === 'PDF') this.generarPDF();
        else this.generarExcel();
    }

    private generarPDF() {
        const doc = new jsPDF();
        const titulo = this.obtenerNombreReporte();

        // Header
        doc.setFontSize(18);
        doc.text('Sistema de Gestión de Ayudantías de Cátedra', 14, 20);
        doc.setFontSize(14);
        doc.setTextColor(100);
        doc.text(titulo, 14, 30);
        doc.setFontSize(10);
        doc.text(`Fecha de generación: ${new Date().toLocaleString()}`, 14, 38);
        doc.text(`Facultad ID: ${this.idFacultad}`, 14, 43);

        if (this.activeReport === 'RESUMEN_EJECUTIVO') {
            this.addExecutiveSummary(doc);
        } else {
            const data = this.obtenerDataFiltradaTotal();
            const head = this.activeReport.startsWith('CONVOCATORIAS')
                ? [['Asignatura', 'Carrera', 'Coordinador', 'Estado', 'Postulantes']]
                : [['Estudiante', 'Cédula', 'Asignatura', 'Estado Evaluación']];

            const body = this.activeReport.startsWith('CONVOCATORIAS')
                ? data.map(c => [c.nombreAsignatura, c.nombreCarrera, c.nombreCoordinador, c.estado, c.numeroPostulantes])
                : data.map(p => [p.nombreEstudiante, p.cedula, p.nombreAsignatura, p.estadoEvaluacion]);

            autoTable(doc, {
                head: head,
                body: body,
                startY: 50,
                theme: 'striped',
                headStyles: { fillColor: [67, 56, 202] }
            });
        }

        doc.save(`${titulo.replace(/ /g, '_')}.pdf`);
    }

    private addExecutiveSummary(doc: jsPDF) {
        if (!this.estadisticas) return;
        doc.setFontSize(12);
        doc.setTextColor(0);
        doc.text('Resumen de Gestión de Facultad', 14, 55);

        const statsData = [
            ['Categoría', 'Valor'],
            ['Total Convocatorias', this.estadisticas.totalConvocatorias.toString()],
            ['Convocatorias Activas', this.estadisticas.convocatoriasActivas.toString()],
            ['Convocatorias Cerradas', this.estadisticas.convocatoriasInactivas.toString()],
            ['Total Postulantes', this.estadisticas.totalPostulantes.toString()],
            ['Aprobados (Seleccionados)', this.estadisticas.postulantesSeleccionados.toString()],
            ['Rechazados (No Seleccionados)', this.estadisticas.postulantesNoSeleccionados.toString()],
            ['En Evaluación', this.estadisticas.postulantesEnEvaluacion.toString()]
        ];

        autoTable(doc, {
            body: statsData,
            startY: 60,
            theme: 'grid',
            styles: { cellPadding: 5 }
        });

        if (this.estadisticas.actividadPorCoordinador.length > 0) {
            doc.text('Distribución por Coordinador (Mayores a 0)', 14, (doc as any).lastAutoTable.finalY + 15);
            autoTable(doc, {
                head: [['Coordinador', 'Postulaciones Gestionadas']],
                body: this.estadisticas.actividadPorCoordinador.map(a => [a.nombreCoordinador, a.totalConvocatorias]),
                startY: (doc as any).lastAutoTable.finalY + 20,
                headStyles: { fillColor: [45, 55, 72] }
            });
        }
    }

    private generarExcel() {
        const data = this.obtenerDataFiltradaTotal();
        const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(data);
        const wb: XLSX.WorkBook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Reporte');
        XLSX.writeFile(wb, `${this.obtenerNombreReporte().replace(/ /g, '_')}.xlsx`);
    }

    private obtenerNombreReporte(): string {
        const map: Record<ReportType, string> = {
            'CONVOCATORIAS_GENERAL': 'Reporte General de Convocatorias',
            'CONVOCATORIAS_ESTADO': 'Convocatorias Agrupadas por Estado',
            'POSTULANTES_GENERAL': 'Reporte Listado Maestro de Postulantes',
            'POSTULANTES_VERSUS': 'Comparativa Aprobados vs Rechazados',
            'POSTULANTES_DESGLOSE': 'Desglose de Postulantes por Asignatura',
            'RESUMEN_EJECUTIVO': 'Resumen Ejecutivo Institucional'
        };
        return map[this.activeReport];
    }

    private obtenerDataFiltradaTotal(): any[] {
        if (this.activeReport.startsWith('CONVOCATORIAS')) {
            return this.activeReport === 'CONVOCATORIAS_ESTADO'
                ? [...this.convocatorias].sort((a, b) => a.estado.localeCompare(b.estado))
                : this.convocatorias;
        }
        if (this.activeReport === 'POSTULANTES_VERSUS') {
            return this.postulantes.filter(p => p.estadoEvaluacion === 'SELECCIONADO' || p.estadoEvaluacion === 'NO_SELECCIONADO');
        }
        if (this.activeReport === 'POSTULANTES_DESGLOSE') {
            return [...this.postulantes].sort((a, b) => a.nombreAsignatura.localeCompare(b.nombreAsignatura));
        }
        return this.postulantes;
    }
}
