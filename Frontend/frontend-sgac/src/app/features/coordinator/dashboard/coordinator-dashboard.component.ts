import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Users, FileText, CheckCircle, AlertCircle, Clock } from 'lucide-angular';
import {PostulacionService} from '../../../core/services/postulacion-service';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';


@Component({
    selector: 'app-coordinator-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './coordinator-dashboard.component.html'
})
export class CoordinatorDashboardComponent implements OnInit {
    private convocatoriasService = inject(ConvocatoriaService);
    private postulacionService = inject(PostulacionService);

    stats = signal({
        pending: 12,
        reviewed: 45,
        observed: 5,
        total: 62
    });

    urgentTasks = [
        { id: 1, title: 'Revisar postulación de Juan Pérez', type: 'Review', date: 'Hace 2 horas', status: 'pending' },
        { id: 2, title: 'Convocatoria "Ayudantía Física I" por cerrar', type: 'Deadline', date: 'Mañana', status: 'warning' },
        { id: 3, title: '3 postulaciones nuevas en "Programación Web"', type: 'New', date: 'Hoy', status: 'info' }
    ];

    readonly Users = Users;
    readonly FileText = FileText;
    readonly CheckCircle = CheckCircle;
    readonly AlertCircle = AlertCircle;
    readonly Clock = Clock;

    ngOnInit() {

    }
}
