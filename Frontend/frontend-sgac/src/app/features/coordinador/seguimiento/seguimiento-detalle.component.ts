import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, LUCIDE_ICONS, LucideIconProvider, 
  ChevronDown, ChevronUp, CheckCircle, XCircle, Clock, Eye, ArrowLeft, Send, FileText, CheckSquare, MessageCircle, Paperclip, FolderOpen
} from 'lucide-angular';
import { Subscription } from 'rxjs';

import { DocenteService } from '../../../core/services/docente-service';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { InformeMensualService, InformeMensual } from '../../../core/services/informe-mensual-service';
import { RegistroActividadDocenteDTO } from '../../../core/dto/docente';
import { RequisitoAdjuntoResponseDTO } from '../../../core/dto/postulacion';
import { ChatInternoComponent } from '../comunicacion/chat-interno/chat-interno.component';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, ChatInternoComponent, RouterModule],
  providers: [
    { 
      provide: LUCIDE_ICONS, 
      multi: true, 
      useValue: new LucideIconProvider({ 
        ChevronDown, ChevronUp, CheckCircle, XCircle, Clock, Eye, ArrowLeft, Send, FileText, CheckSquare, MessageCircle, Paperclip, FolderOpen
      }) 
    }
  ],
  templateUrl: './seguimiento-detalle.html',
  styleUrl: './seguimiento-detalle.css'
})
export class SeguimientoDetalleComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private docenteService = inject(DocenteService);
  private coordinadorService = inject(CoordinadorService);
  private informeService = inject(InformeMensualService);
  private subs = new Subscription();

  idAyudantia!: number;
  idPostulacion: number | null = null;
  actividades: RegistroActividadDocenteDTO[] = [];
  informes: InformeMensual[] = [];
  documentos: RequisitoAdjuntoResponseDTO[] = [];
  
  loading = true;
  loadingInformes = false;
  loadingDocumentos = false;

  tabActiva: 'sesiones' | 'chat' | 'informes' | 'documentos' = 'sesiones';

  ngOnInit(): void {
    this.idAyudantia = Number(this.route.snapshot.paramMap.get('idAyudantia'));
    const tabParam = this.route.snapshot.queryParamMap.get('tab');
    if (tabParam === 'chat' || tabParam === 'informes') {
      this.tabActiva = tabParam as any;
    }
    
    if (!this.idAyudantia) {
      this.router.navigate(['/coordinador/seguimiento']);
      return;
    }
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  cargarDatos(): void {
    this.loading = true;
    this.subs.add(
      this.coordinadorService.obtenerDetalleAyudantiaCompleto(this.idAyudantia).subscribe({
        next: (res) => {
          this.idPostulacion = res.idPostulacion;
          if (res.actividades) {
            this.actividades = res.actividades.map((a: any) => ({
              ...a,
              fecha: a.fecha ? new Date(a.fecha[0], a.fecha[1] - 1, a.fecha[2]).toISOString() : null
            }));
          }
          this.loading = false;
          
          // Si iniciamos en tab documentos, cargar ahora que tenemos idPostulacion
          if (this.tabActiva === 'documentos') this.cargarDocumentos();
        },
        error: () => {
          this.docenteService.getActividadesAyudante(this.idAyudantia).subscribe({
            next: (res) => { this.actividades = res; this.loading = false; },
            error: () => this.loading = false
          });
        }
      })
    );
  }

  cargarInformes(): void {
    this.loadingInformes = true;
    // Lógica futura de informes
    this.loadingInformes = false;
  }

  cargarDocumentos(): void {
    if (!this.idPostulacion) return;
    this.loadingDocumentos = true;
    this.subs.add(
      this.coordinadorService.listarDocumentosPorPostulacion(this.idPostulacion).subscribe({
        next: (docs) => {
          this.documentos = docs || [];
          this.loadingDocumentos = false;
        },
        error: () => {
          this.documentos = [];
          this.loadingDocumentos = false;
        }
      })
    );
  }

  cambiarTab(tab: 'sesiones' | 'chat' | 'informes' | 'documentos'): void {
    this.tabActiva = tab;
    if (tab === 'informes') this.cargarInformes();
    if (tab === 'documentos') this.cargarDocumentos();
  }

  verDocumento(idRequisito: number): void {
    const url = this.coordinadorService.getUrlDescargaDocumento(idRequisito);
    window.open(url, '_blank');
  }

  aprobarInforme(idInforme: number): void {
    if (!confirm('¿Está seguro de aprobar este informe como Coordinador?')) return;
    this.subs.add(
      this.informeService.aprobarCoordinador(idInforme).subscribe({
        next: () => {
          alert('Informe aprobado exitosamente');
          this.cargarInformes();
        },
        error: (err) => alert('Error: ' + err.message)
      })
    );
  }

  toggleActividad(act: any): void {
    act.expandido = !act.expandido;
  }

  volver(): void {
    this.router.navigate(['/coordinador/seguimiento']);
  }

  getEstadoClass(estado: string): string {
    const e = (estado || '').toUpperCase();
    if (e.includes('APROB') || e.includes('ACEPT')) return 'estado-aceptado';
    if (e.includes('RECHAZ')) return 'estado-rechazado';
    if (e.includes('OBSERV')) return 'estado-observado';
    return 'estado-pendiente';
  }
}
