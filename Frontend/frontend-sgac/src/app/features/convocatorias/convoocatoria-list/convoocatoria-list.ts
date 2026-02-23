import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LucideAngularModule, Search, LayoutGrid, List, Filter } from 'lucide-angular';
import {ConvocatoriaCardComponent} from '../convocatoria-card/convocatoria-card';
import {ConvocatoriaDetailModalComponent} from '../convocatoria-detail-modal/convocatoria-detail-modal';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';
import {PostulacionService} from '../../../core/services/postulacion-service';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';
import {FiltersBarComponent} from '../../../shared/components/filters-bar/filters-bar.component';

@Component({
  selector: 'app-convocatorias-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideAngularModule,
    ConvocatoriaCardComponent,
    ConvocatoriaDetailModalComponent,
    FiltersBarComponent
  ],
  templateUrl: './convoocatoria-list.html',
  styleUrl: './convoocatoria-list.css'
})
export class ConvocatoriasListComponent implements OnInit {
  private convocatoriasService = inject(ConvocatoriaService);
  private postulacionService = inject(PostulacionService);
  private router = inject(Router);

  readonly Search = Search;
  readonly LayoutGrid = LayoutGrid;
  readonly List = List;
  readonly Filter = Filter;

  convocatorias = signal<ConvocatoriaDTO[]>([]);
  loading = signal(false);
  searchQuery = signal('');
  showOpenOnly = signal(true);
  selectedCarrera = signal('all');
  selectedConvocatoria = signal<ConvocatoriaDTO | null>(null);
  appliedConvocatoriaIds = signal<Set<number>>(new Set());

  filteredConvocatorias = computed(() => {
    return this.convocatorias().filter(c => {
      const matchesSearch = c.nombreAsignatura?.toLowerCase().includes(this.searchQuery().toLowerCase());
      const matchesStatus = this.showOpenOnly() ? c.estado === 'ABIERTA' : true;
      const matchesCarrera = this.selectedCarrera() === 'all' ? true : c.idAsignatura.toString() === this.selectedCarrera();

      return matchesSearch && matchesStatus && matchesCarrera;
    });
  });

  ngOnInit(): void {
    this.loadConvocatorias();
    this.loadPostulaciones();
  }

  loadConvocatorias(): void {
    this.loading.set(true);
    this.convocatoriasService.getAll().subscribe({
      next: (data) => {
        this.convocatorias.set(data);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading convocatorias:', error);
        this.loading.set(false);
      }
    });
  }

  loadPostulaciones(): void {
    const idEstudiante = 1;
    this.postulacionService.listarPorEstudiante(idEstudiante).subscribe({
      next: (postulaciones) => {
        const ids = new Set(postulaciones.map(p => p.idConvocatoria));
        this.appliedConvocatoriaIds.set(ids);
      },
      error: (err) => console.error('Error loading postulaciones', err)
    });
  }

  onViewDetails(convocatoria: ConvocatoriaDTO): void {
    this.selectedConvocatoria.set(convocatoria);
  }

  onCloseDetail(): void {
    this.selectedConvocatoria.set(null);
  }

  onPostular(id: number): void {
    console.log('Postular a convocatoria:', id);
    this.onCloseDetail();
    this.router.navigate(['/student/postulaciones/postular', id]);
  }

  onVerEstado(idConvocatoria: number): void {
    this.onCloseDetail();
    this.router.navigate(['/student/postulaciones/mis-postulaciones']);
  }

  isApplied(idConvocatoria: number): boolean {
    return this.appliedConvocatoriaIds().has(idConvocatoria);
  }
}
