import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, FileText, CheckCircle, Clock, AlertCircle, Plus } from 'lucide-angular';
import {ConvocatoriaService} from '../../../core/services/convocatoria-service';

@Component({
    selector: 'app-dean-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule, LucideAngularModule],
    templateUrl: './dean-dashboard.component.html'
})
export class DeanDashboardComponent implements OnInit {
    private convocatoriasService = inject(ConvocatoriaService);

    stats = signal({
        active: 3,
        draft: 1,
        review: 2,
        total: 6
    });

    // Icons
    readonly FileText = FileText;
    readonly CheckCircle = CheckCircle;
    readonly Clock = Clock;
    readonly AlertCircle = AlertCircle;
    readonly Plus = Plus;

    ngOnInit() {
        // TODO: Fetch real stats
    }
}
