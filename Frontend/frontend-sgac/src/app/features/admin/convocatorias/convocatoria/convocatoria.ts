import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {
  BookOpen,
  Calendar, GraduationCap,
  LUCIDE_ICONS,
  LucideAngularModule,
  LucideIconProvider,
  Save,
  Users,
  X,
  PlusCircle,
  Search
} from 'lucide-angular';
import {ConvocatoriaService} from '../../../../core/services/convocatoria-service';
import {ActivatedRoute, Router} from '@angular/router';
import {ConvocatoriaDTO} from '../../../../core/dto/convocatoria';
import {ConvocatoriaCardComponent} from '../../../convocatorias/convocatoria-card/convocatoria-card';
import {
  ConvocatoriaDetailModalComponent
} from '../../../convocatorias/convocatoria-detail-modal/convocatoria-detail-modal';
import { ConvocatoriaFormModalComponent} from '../convocatoria-form-modal/convocatoria-form-modal';

@Component({
  selector: 'app-convocatoria-form-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideAngularModule,
    ConvocatoriaCardComponent,
    ConvocatoriaDetailModalComponent,
    ConvocatoriaFormModalComponent
  ],
  providers: [
    {
      provide: LUCIDE_ICONS,
      multi: true,
      useValue: new LucideIconProvider({ X, Save, Calendar, Users, BookOpen, GraduationCap })
    }
  ],
  templateUrl: './convocatoria.html',
  styleUrl: './convocatoria.css',
})
export class ConvocatoriaComponent {
  private convocatoriasService = inject(ConvocatoriaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly PlusCircle = PlusCircle;
  readonly Search = Search;

  // State
  convocatorias = signal<ConvocatoriaDTO[]>([]);
  loading = signal(false);
  searchQuery = signal('');
  showCreateModal = signal(false);
  showEditModal = signal(false);
  selectedConvocatoria = signal<ConvocatoriaDTO | null>(null);
  convocatoriaToEdit = signal<ConvocatoriaDTO | null>(null);

  ngOnInit(): void {
    this.loadConvocatorias();
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

  onOpenCreate(): void {
    this.showCreateModal.set(true);
  }

  onCloseCreate(): void {
    this.showCreateModal.set(false);
    this.loadConvocatorias();
  }

  onViewDetails(convocatoria: ConvocatoriaDTO): void {
    this.selectedConvocatoria.set(convocatoria);
  }

  onCloseDetail(): void {
    this.selectedConvocatoria.set(null);
  }

  onEdit(convocatoria: ConvocatoriaDTO): void {
    this.convocatoriaToEdit.set(convocatoria);
    this.showEditModal.set(true);
  }

  onCloseEdit(): void {
    this.showEditModal.set(false);
    this.convocatoriaToEdit.set(null);
    this.loadConvocatorias();
  }

  onDelete(id: number): void {
    if (confirm('¿Está seguro de eliminar esta convocatoria?')) {
      this.convocatoriasService.delete(id).subscribe({
        next: () => {
          alert('Convocatoria eliminada correctamente');
          this.loadConvocatorias();
        },
        error: (error) => {
          console.error('Error deleting convocatoria:', error);
          alert('Error al eliminar la convocatoria');
        }
      });
    }
  }

  onVerifyPostulantes(id: number): void {
    this.router.navigate(['./', id, 'postulantes'], { relativeTo: this.route });
    this.onCloseDetail();
  }
}
