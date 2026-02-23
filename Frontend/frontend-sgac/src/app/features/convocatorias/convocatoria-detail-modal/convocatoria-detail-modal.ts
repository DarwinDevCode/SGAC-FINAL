import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, X, Calendar, Users, Clock, FileText } from 'lucide-angular';
import {AuthService} from '../../../core/services/auth-service';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';

@Component({
  selector: 'app-convocatoria-detail-modal',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './convocatoria-detail-modal.html',
  styleUrl: './convocatoria-detail-modal.css'
})
export class ConvocatoriaDetailModalComponent {
  private authService = inject(AuthService);

  @Input({ required: true }) convocatoria!: ConvocatoriaDTO;
  @Input() isApplied: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() postular = new EventEmitter<number>();
  @Output() verifyPostulantes = new EventEmitter<number>();
  @Output() verEstado = new EventEmitter<number>();

  readonly X = X;
  readonly Calendar = Calendar;
  readonly Users = Users;
  readonly Clock = Clock;
  readonly FileText = FileText;

  get isStaff(): boolean {
    return this.authService.hasRole(['COORDINADOR', 'DECANO', 'ADMINISTRADOR']);
  }

  get isStudent(): boolean {
    return this.authService.hasRole(['ESTUDIANTE']);
  }

  onClose(): void {
    this.close.emit();
  }

  onPostular(): void {
    this.postular.emit(this.convocatoria.idConvocatoria);
  }

  onVerEstado(): void {
    this.verEstado.emit(this.convocatoria.idConvocatoria);
  }

  onVerifyPostulantes(): void {
    this.verifyPostulantes.emit(this.convocatoria.idConvocatoria);
  }

  getStatusColor(): string {
    switch (this.convocatoria.estado) {
      case 'ABIERTA':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'CERRADA':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'EN_PROCESO':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  }
}
