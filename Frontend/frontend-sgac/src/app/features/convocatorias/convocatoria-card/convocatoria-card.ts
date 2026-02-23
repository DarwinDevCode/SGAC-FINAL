import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Calendar, Users, Clock } from 'lucide-angular';
import {ConvocatoriaDTO} from '../../../core/dto/convocatoria';
import {StatusChipComponent} from '../../../shared/components/status-chip/status-chip.component';

@Component({
  selector: 'app-convocatoria-card',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, StatusChipComponent],
  templateUrl: './convocatoria-card.html',
  styleUrl: './convocatoria-card.css'
})
export class ConvocatoriaCardComponent {
  @Input({ required: true }) convocatoria!: ConvocatoriaDTO;
  @Output() viewDetails = new EventEmitter<ConvocatoriaDTO>();

  readonly Calendar = Calendar;
  readonly Users = Users;
  readonly Clock = Clock;

  onViewDetails(): void {
    this.viewDetails.emit(this.convocatoria);
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
