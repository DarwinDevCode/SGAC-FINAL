import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth-service';
import { RouterModule, ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-coordinador-seguimiento',
    standalone: true,
    imports: [CommonModule, LucideAngularModule, RouterModule],
    templateUrl: './seguimiento.html',
    styleUrl: './seguimiento.css',
})
export class SeguimientoComponent implements OnInit, OnDestroy {
    private route = inject(ActivatedRoute);
    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private subs = new Subscription();

    ayudantes: any[] = [];
    loading = true;
    errorMensaje = '';
    esModoChat = false;

    ngOnInit(): void {
        this.subs.add(
            this.route.queryParamMap.subscribe(params => {
                this.esModoChat = params.get('mode') === 'chat';
            })
        );
        this.cargarAyudantes(); 
    }
    ngOnDestroy(): void { this.subs.unsubscribe(); }

    cargarAyudantes() {
        this.loading = true;
        this.subs.add(
            this.http.get<any[]>(`${environment.apiUrl}/ayudantes/listar`).subscribe({
                next: (data) => { this.ayudantes = data || []; this.loading = false; },
                error: () => { this.errorMensaje = 'No se pudieron cargar los ayudantes.'; this.loading = false; }
            })
        );
    }
}
