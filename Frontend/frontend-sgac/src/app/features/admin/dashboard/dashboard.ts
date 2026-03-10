import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    LucideAngularModule,
    LUCIDE_ICONS,
    LucideIconProvider,
    Users,
    UserCheck,
    Shield,
    CalendarClock,
    Activity,
    HardDrive,
    ChevronRight,
    Inbox
} from 'lucide-angular';
import { UsuarioService } from '../../../core/services/usuario-service';
import { PeriodoAcademicoService } from '../../../core/services/periodo-academico-service';
import { UsuarioDTO } from '../../../core/dto/usuario';
import { PeriodoAcademicoDTO } from '../../../core/dto/periodo-academico';
import { Subscription } from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [CommonModule, LucideAngularModule, RouterLink],
    templateUrl: './dashboard.html',
    styleUrl: './dashboard.css',
    providers: [
        {
            provide: LUCIDE_ICONS,
            multi: true,
            useValue: new LucideIconProvider({
                Users,
                UserCheck,
                Shield,
                CalendarClock,
                Activity,
                HardDrive,
                ChevronRight,
                Inbox
            })
        }
    ]
})
export class DashboardComponent implements OnInit, OnDestroy {
    private usuarioService = inject(UsuarioService);
    private periodoService = inject(PeriodoAcademicoService);
    private subs = new Subscription();

    usuarios: UsuarioDTO[] = [];
    periodos: PeriodoAcademicoDTO[] = [];

    loadingUsuarios = true;
    loadingPeriodos = true;

    // Métricas
    totalUsuarios = 0;
    usuariosActivos = 0;
    totalPeriodos = 0;
    periodoActivo: PeriodoAcademicoDTO | null = null;

    rolesCount: { rol: string; cuenta: number; color: string }[] = [];

    ngOnInit() {
        this.cargarDatos();
    }

    ngOnDestroy() {
        this.subs.unsubscribe();
    }

    cargarDatos() {
        // 1. Cargar Usuarios
        this.subs.add(
            this.usuarioService.listarUsuarios().subscribe({
                next: (users: UsuarioDTO[]) => {
                    this.usuarios = users;
                    this.totalUsuarios = users.length;
                    this.usuariosActivos = users.filter((u: UsuarioDTO) => u.activo).length;
                    this.calcularDesgloseRoles(users);
                    this.loadingUsuarios = false;
                },
                error: (err: any) => {
                    console.error('Error al cargar usuarios:', err);
                    this.loadingUsuarios = false;
                }
            })
        );

        // 2. Cargar Periodos
        this.subs.add(
            this.periodoService.listarTodos().subscribe({
                next: (periodosList: PeriodoAcademicoDTO[]) => {
                    this.periodos = periodosList;
                    this.totalPeriodos = periodosList.length;
                    this.periodoActivo = periodosList.find((p: PeriodoAcademicoDTO) => p.estado === 'ACTIVO') || null;
                    this.loadingPeriodos = false;
                },
                error: (err: any) => {
                    console.error('Error al cargar periodos:', err);
                    this.loadingPeriodos = false;
                }
            })
        );
    }

    calcularDesgloseRoles(users: UsuarioDTO[]) {
        const counts: Record<string, number> = {};
        users.forEach(u => {
            // Tomamos el primer rol (o el rolActual/Registro si aplica)
            const rol = u.roles && u.roles.length > 0 ? u.roles[0].nombreTipoRol : (u.rolRegistro || 'DESCONOCIDO');
            counts[rol] = (counts[rol] || 0) + 1;
        });

        const coloresRef: Record<string, string> = {
            'ESTUDIANTE': 'badge blue',
            'DOCENTE': 'badge green',
            'COORDINADOR': 'badge violet',
            'DECANO': 'badge amber',
            'ADMINISTRADOR': 'badge red',
            'AYUDANTE_CATEDRA': 'status-pill info'
        };

        this.rolesCount = Object.keys(counts).map(rol => ({
            rol: rol.replace('ROLE_', ''),
            cuenta: counts[rol],
            color: coloresRef[rol] || 'bg-gray-100 text-gray-700'
        })).sort((a, b) => b.cuenta - a.cuenta);
    }
}
