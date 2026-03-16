import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { AdminReporteService } from '../../../core/services/admin-reporte.service';
import { CatalogosService } from '../../../core/services/catalogos-service';
import { PeriodoAcademicoService } from '../../../core/services/periodo-academico-service';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-reportes-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './reportes-admin.html',
  styleUrl: './reportes-admin.css'
})
export class ReportesAdminComponent implements OnInit {
  reporteService = inject(AdminReporteService);
  catalogoService = inject(CatalogosService);
  periodoService = inject(PeriodoAcademicoService);

  // Tipos de Reporte
  tiposReporte = [
    { id: 'auditoria', nombre: 'Log de Auditoría (Actividad Reciente)' },
    { id: 'facultades', nombre: 'Reporte de Facultades' },
    { id: 'carreras', nombre: 'Reporte de Carreras' },
    { id: 'asignaturas', nombre: 'Reporte de Asignaturas' },
    { id: 'convocatorias', nombre: 'Reporte de Convocatorias' },
    { id: 'personal', nombre: 'Reporte de Decanos y Coordinadores' },
    { id: 'postulantes', nombre: 'Reporte Global de Postulantes' },
    { id: 'usuarios', nombre: 'Reporte de Usuarios' }
  ];

  selectedTipo = 'auditoria';
  data: any[] = [];
  loading = false;

  // Filtros
  filtros: any = {
    idFacultad: null,
    idCarrera: null,
    idAsignatura: null,
    idPeriodo: null,
    idUsuario: null,
    modulo: '',
    tipoPersonal: '',
    rolUsuario: '',
    estadoPostulacion: '',
    activoUsuario: null
  };

  // Catálogos para filtros
  facultades: any[] = [];
  carreras: any[] = [];
  asignaturas: any[] = [];
  periodos: any[] = [];

  ngOnInit() {
    this.cargarFacultades();
    this.cargarPeriodos();
    this.generarReporte();
  }

  cargarFacultades() {
    this.catalogoService.getFacultades().subscribe(res => this.facultades = res);
  }

  cargarPeriodos() {
    this.periodoService.listarTodos().subscribe(res => this.periodos = res);
  }

  onFacultadChange() {
    this.filtros.idCarrera = null;
    this.filtros.idAsignatura = null;
    this.carreras = [];
    this.asignaturas = [];
    if (this.filtros.idFacultad) {
      this.reporteService.getCarreras(this.filtros.idFacultad).subscribe(res => this.carreras = res);
    }
    this.generarReporte();
  }

  onCarreraChange() {
    this.filtros.idAsignatura = null;
    this.asignaturas = [];
    if (this.filtros.idCarrera) {
      this.reporteService.getAsignaturas(this.filtros.idCarrera).subscribe(res => this.asignaturas = res);
    }
    this.generarReporte();
  }

  generarReporte() {
    this.loading = true;
    let obs;

    switch (this.selectedTipo) {
      case 'auditoria':
        obs = this.reporteService.getAuditoria(this.filtros);
        break;
      case 'facultades':
        obs = this.reporteService.getFacultades();
        break;
      case 'carreras':
        obs = this.reporteService.getCarreras(this.filtros.idFacultad);
        break;
      case 'asignaturas':
        obs = this.reporteService.getAsignaturas(this.filtros.idCarrera);
        break;
      case 'convocatorias':
        obs = this.reporteService.getConvocatorias(this.filtros.idAsignatura, this.filtros.idPeriodo);
        break;
      case 'personal':
        obs = this.reporteService.getPersonal({ 
          idFacultad: this.filtros.idFacultad, 
          idCarrera: this.filtros.idCarrera,
          tipo: this.filtros.tipoPersonal 
        });
        break;
      case 'postulantes':
        obs = this.reporteService.getPostulantes({
          idAsignatura: this.filtros.idAsignatura,
          idPeriodo: this.filtros.idPeriodo,
          estado: this.filtros.estadoPostulacion
        });
        break;
      case 'usuarios':
        obs = this.reporteService.getUsuarios({
          rol: this.filtros.rolUsuario,
          activo: this.filtros.activoUsuario
        });
        break;
    }

    if (obs) {
      obs.subscribe({
        next: (res) => {
          this.data = res;
          this.loading = false;
        },
        error: () => this.loading = false
      });
    }
  }

  exportarPDF() {
    const doc = new jsPDF();
    const title = this.tiposReporte.find(t => t.id === this.selectedTipo)?.nombre || 'Reporte';
    doc.text(title, 14, 20);

    const columns = this.getColumns();
    const rows = this.data.map(item => columns.map(col => item[col.key]));

    autoTable(doc, {
      head: [columns.map(col => col.label)],
      body: rows,
      startY: 30,
    });

    doc.save(`${this.selectedTipo}_${new Date().getTime()}.pdf`);
  }

  exportarExcel() {
    const worksheet = XLSX.utils.json_to_sheet(this.data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Reporte");
    XLSX.writeFile(workbook, `${this.selectedTipo}_${new Date().getTime()}.xlsx`);
  }

  getColumns() {
    switch (this.selectedTipo) {
      case 'auditoria':
        return [
          { label: 'Fecha', key: 'fechaHora' },
          { label: 'Usuario', key: 'nombreUsuario' },
          { label: 'Acción', key: 'accion' },
          { label: 'Módulo', key: 'tablaAfectada' }
        ];
      case 'facultades':
      case 'carreras':
      case 'asignaturas':
      case 'convocatorias':
        return [
          { label: 'Nombre', key: 'nombre' },
          { label: 'Contexto', key: 'descripcion' },
          { label: 'Relacionado', key: 'totalRelacionado' },
          { label: 'Estado', key: 'estado' }
        ];
      case 'personal':
        return [
          { label: 'Nombre', key: 'nombre' },
          { label: 'Cargo/Contexto', key: 'descripcion' },
          { label: 'Estado', key: 'estado' }
        ];
      case 'postulantes':
        return [
          { label: 'Estudiante', key: 'nombreEstudiante' },
          { label: 'Cédula', key: 'cedula' },
          { label: 'Asignatura', key: 'nombreAsignatura' },
          { label: 'Periodo', key: 'nombrePeriodo' },
          { label: 'Estado', key: 'estadoEvaluacion' }
        ];
      case 'usuarios':
        return [
          { label: 'Usuario', key: 'nombreUsuario' },
          { label: 'Email', key: 'correo' },
          { label: 'Roles', key: 'roles' },
          { label: 'Estado', key: 'activo' }
        ];
      default: return [];
    }
  }
}
