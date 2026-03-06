import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { HttpClient } from '@angular/common/http';
import { Subscription, switchMap, of } from 'rxjs';
import { CoordinadorService } from '../../../core/services/coordinador-service';
import { AuthService } from '../../../core/services/auth-service';

type Tab = 'meritos' | 'oposicion' | 'resultados' | 'actas';

@Component({
    selector: 'app-coordinador-evaluaciones',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, LucideAngularModule],
    templateUrl: './evaluaciones.html',
    styleUrl: './evaluaciones.css',
})
export class EvaluacionesComponent implements OnInit, OnDestroy {
    private fb = inject(FormBuilder);
    private coordinadorService = inject(CoordinadorService);
    private authService = inject(AuthService);
    private http = inject(HttpClient);
    private subs = new Subscription();

    private readonly BASE = 'http://localhost:8080/api';

    // ─── Estado general ─────────────────────────────────────────────────────
    activeTab: Tab = 'meritos';
    userRol = '';
    userId = 0;
    convocatorias: any[] = [];
    idConvSeleccionada = 0;
    loadingConv = true;
    successMsg = '';
    errorMsg = '';

    // ─── Tab 1: Méritos ──────────────────────────────────────────────────────
    postulantes: any[] = [];
    loadingPostulantes = false;
    postulanteSeleccionado: any = null;
    meritosForm!: FormGroup;
    guardandoMeritos = false;
    meritosGuardados: Record<number, any> = {};
    temaSorteado: Record<number, string> = {};

    // ─── Tab 2: Oposición ────────────────────────────────────────────────────
    postulanteOposicion: any = null;
    oposicionForm!: FormGroup;
    guardandoOposicion = false;
    estadoOposicion: any = null;
    loadingEstado = false;

    // ─── Tab 3: Resultados ───────────────────────────────────────────────────
    ranking: any[] = [];
    loadingRanking = false;
    hayEmpate = false;

    // ─── Tab 4: Actas ────────────────────────────────────────────────────────
    actasMeritos: any = null;
    actasOposicion: any = null;
    generandoActa = false;
    confirmandoActa = false;
    postulanteActas: any = null;

    // ─── Computed ────────────────────────────────────────────────────────────
    get totalMeritos(): number {
        if (!this.meritosForm) return 0;
        const v = this.meritosForm.value;
        return (+(v.calificacionAsignatura || 0))
            + (+(v.promedioPorSemestres || 0))
            + (+(v.experienciaColaboracion || 0))
            + (+(v.participacionEventos || 0));
    }

    get subtotalOposicion(): number {
        if (!this.oposicionForm) return 0;
        const ctrl = this.oposicionForm.get(this.rolKey);
        if (!ctrl) return 0;
        const v = ctrl.value;
        return (+(v.material || 0)) + (+(v.calidad || 0)) + (+(v.pertinencia || 0));
    }

    get rolKey(): string {
        if (this.userRol === 'DECANO') return 'decano';
        if (this.userRol === 'DOCENTE') return 'docente';
        return 'coordinador';
    }

    /** El DECANO solo puede ver méritos en read-only y ver actas/resultados — no puede calificar oposición */
    get puedeCalificarMeritos(): boolean { return this.userRol !== 'DECANO'; }
    get puedeCalificarOposicion(): boolean { return true; } // Los 3 pueden

    ngOnInit(): void {
        const user = this.authService.getUser();
        if (!user) { this.loadingConv = false; return; }
        this.userId = user.idUsuario;
        this.userRol = this.normalizeRol(user.rolActual);

        this.buildForms();
        this.cargarConvocatoriasSegunRol(user);
    }

    ngOnDestroy(): void { this.subs.unsubscribe(); }

    /**
     * Carga las convocatorias filtradas:
     * - COORDINADOR: convocatorias de su carrera (CoordinadorService)
     * - DOCENTE / DECANO: todas las convocatorias activas (endpoint general)
     */
    private cargarConvocatoriasSegunRol(user: any): void {
        if (this.userRol === 'COORDINADOR') {
            this.subs.add(
                this.coordinadorService.obtenerCoordinadorPorUsuario(user.idUsuario).pipe(
                    switchMap((coord: any) =>
                        this.coordinadorService.listarConvocatoriasPorCarrera(coord.idCarrera)
                    )
                ).subscribe({
                    next: (convs: any[]) => { this.convocatorias = convs; this.loadingConv = false; },
                    error: () => { this.loadingConv = false; }
                })
            );
        } else {
            // DOCENTE y DECANO: todas las convocatorias activas
            this.subs.add(
                this.http.get<any[]>(`${this.BASE}/convocatorias/listar-vista`).subscribe({
                    next: (convs) => { this.convocatorias = convs || []; this.loadingConv = false; },
                    error: () => { this.loadingConv = false; }
                })
            );
        }
    }

    private normalizeRol(raw?: string | null): string {
        if (!raw) return 'COORDINADOR';
        return raw.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toUpperCase().replace(/^ROLE_/, '');
    }

    private buildForms(): void {
        this.meritosForm = this.fb.group({
            calificacionAsignatura: [null, [Validators.required, Validators.min(0), Validators.max(10)]],
            promedioPorSemestres: [null, [Validators.required, Validators.min(0), Validators.max(4)]],
            experienciaColaboracion: [null, [Validators.required, Validators.min(0), Validators.max(4)]],
            participacionEventos: [null, [Validators.required, Validators.min(0), Validators.max(2)]],
        }, { validators: this.totalMeritosValidator });

        const bloqueOposicion = () => this.fb.group({
            material: [null, [Validators.required, Validators.min(0), Validators.max(10)]],
            calidad: [null, [Validators.required, Validators.min(0), Validators.max(4)]],
            pertinencia: [null, [Validators.required, Validators.min(0), Validators.max(6)]],
        });

        this.oposicionForm = this.fb.group({
            decano: bloqueOposicion(),
            coordinador: bloqueOposicion(),
            docente: bloqueOposicion(),
        });
    }

    private totalMeritosValidator = (fg: AbstractControl) => {
        const v = fg.value;
        const total = (+(v.calificacionAsignatura || 0)) + (+(v.promedioPorSemestres || 0))
            + (+(v.experienciaColaboracion || 0)) + (+(v.participacionEventos || 0));
        return total > 20 ? { totalExcede: true } : null;
    };

    // ─── Convocatoria ────────────────────────────────────────────────────────
    onConvocatoriaChange(): void {
        if (!this.idConvSeleccionada) return;
        this.postulanteSeleccionado = null;
        this.postulanteOposicion = null;
        this.ranking = [];
        this.actasMeritos = null;
        this.actasOposicion = null;
        this.cargarPostulantes();
    }

    cargarPostulantes(): void {
        this.loadingPostulantes = true;
        this.subs.add(
            this.http.get<any[]>(`${this.BASE}/postulaciones/convocatoria/${this.idConvSeleccionada}`).subscribe({
                next: (data) => { this.postulantes = data || []; this.loadingPostulantes = false; },
                error: () => { this.loadingPostulantes = false; }
            })
        );
    }

    // ─── Tab 1: Méritos ──────────────────────────────────────────────────────
    seleccionarPostulanteMeritos(p: any): void {
        this.postulanteSeleccionado = p;
        this.meritosForm.reset();
        // Cargar datos existentes si ya fueron evaluados
        const existente = this.meritosGuardados[p.idPostulacion];
        if (existente) {
            this.meritosForm.patchValue({
                calificacionAsignatura: existente.notaAsignatura,
                promedioPorSemestres: existente.notaSemestres,
                experienciaColaboracion: existente.notaExperiencia,
                participacionEventos: existente.notaEventos,
            });
        }
    }

    guardarMeritos(): void {
        if (this.meritosForm.invalid || !this.postulanteSeleccionado) return;
        if (this.totalMeritos > 20) { this.showError('El total de méritos no puede superar 20 puntos.'); return; }
        this.guardandoMeritos = true;
        const v = this.meritosForm.value;
        const body = {
            idPostulacion: this.postulanteSeleccionado.idPostulacion,
            notaAsignatura: v.calificacionAsignatura,
            notaSemestres: v.promedioPorSemestres,
            notaExperiencia: v.experienciaColaboracion,
            notaEventos: v.participacionEventos,
            fechaEvaluacion: new Date().toISOString().split('T')[0],
        };
        this.subs.add(
            this.http.post<any>(`${this.BASE}/evaluaciones/meritos`, body).subscribe({
                next: (res) => {
                    this.guardandoMeritos = false;
                    this.meritosGuardados[this.postulanteSeleccionado!.idPostulacion] = res;
                    // Verificar si hay sorteo
                    this.http.get<any>(`${this.BASE}/evaluaciones/oposicion/estado/${this.postulanteSeleccionado!.idPostulacion}`)
                        .subscribe(estado => {
                            if (estado?.temaSorteado) {
                                this.temaSorteado[this.postulanteSeleccionado!.idPostulacion] = estado.temaSorteado;
                            }
                        });
                    this.showSuccess(`Méritos guardados (Total: ${this.totalMeritos.toFixed(2)} / 20). Se realizó el sorteo del tema de oposición.`);
                    this.postulanteSeleccionado = null;
                    this.meritosForm.reset();
                },
                error: (err) => {
                    this.guardandoMeritos = false;
                    this.showError('Error al guardar méritos: ' + (err.error || err.message));
                }
            })
        );
    }

    // ─── Tab 2: Oposición ────────────────────────────────────────────────────
    seleccionarPostulanteOposicion(p: any): void {
        this.postulanteOposicion = p;
        this.cargarEstadoOposicion(p.idPostulacion);
        // Cargar la nota propia si existe
        this.http.get<any>(`${this.BASE}/evaluaciones/oposicion/${p.idPostulacion}/evaluador/${this.userId}`)
            .subscribe({
                next: (nota) => {
                    if (nota) {
                        const ctrl = this.oposicionForm.get(this.rolKey);
                        ctrl?.patchValue({ material: nota.criterioMaterial, calidad: nota.criterioCalidad, pertinencia: nota.criterioPertinencia });
                    }
                },
                error: () => { } // Sin nota previa
            });
    }

    cargarEstadoOposicion(idPostulacion: number): void {
        this.loadingEstado = true;
        this.subs.add(
            this.http.get<any>(`${this.BASE}/evaluaciones/oposicion/estado/${idPostulacion}`).subscribe({
                next: (estado) => { this.estadoOposicion = estado; this.loadingEstado = false; },
                error: () => { this.loadingEstado = false; }
            })
        );
    }

    guardarOposicion(): void {
        const ctrl = this.oposicionForm.get(this.rolKey);
        if (!ctrl || ctrl.invalid || !this.postulanteOposicion) return;
        const v = ctrl.value;
        const subtotal = (+(v.material || 0)) + (+(v.calidad || 0)) + (+(v.pertinencia || 0));
        if (subtotal > 20) { this.showError('El subtotal de oposición no puede superar 20 puntos.'); return; }

        this.guardandoOposicion = true;
        const body = {
            idPostulacion: this.postulanteOposicion.idPostulacion,
            idEvaluador: this.userId,
            rolEvaluador: this.userRol,
            criterioMaterial: v.material,
            criterioCalidad: v.calidad,
            criterioPertinencia: v.pertinencia,
        };
        this.subs.add(
            this.http.post<any>(`${this.BASE}/evaluaciones/oposicion/individual`, body).subscribe({
                next: () => {
                    this.guardandoOposicion = false;
                    this.showSuccess('Calificación de oposición guardada correctamente.');
                    this.cargarEstadoOposicion(this.postulanteOposicion!.idPostulacion);
                },
                error: (err) => {
                    this.guardandoOposicion = false;
                    this.showError('Error: ' + (err.error || err.message));
                }
            })
        );
    }

    // ─── Tab 3: Resultados ───────────────────────────────────────────────────
    cargarRanking(): void {
        if (!this.idConvSeleccionada) return;
        this.loadingRanking = true;
        this.subs.add(
            this.http.get<any[]>(`${this.BASE}/evaluaciones/ranking/convocatoria/v2/${this.idConvSeleccionada}`).subscribe({
                next: (data) => {
                    this.ranking = data || [];
                    this.hayEmpate = this.ranking.some(r => r.empate);
                    this.loadingRanking = false;
                },
                error: () => { this.loadingRanking = false; }
            })
        );
    }

    getEstadoClass(estado: string): string {
        switch (estado) {
            case 'GANADOR': return 'badge-ganador';
            case 'APTO': return 'badge-apto';
            case 'NO_APTO': return 'badge-noApto';
            case 'DESIERTO': return 'badge-desierto';
            default: return 'badge-gray';
        }
    }

    publicandoResultados = false;

    publicarResultados(): void {
        if (!this.idConvSeleccionada || this.publicandoResultados) return;
        this.publicandoResultados = true;
        this.subs.add(
            this.http.post<any>(`${this.BASE}/evaluaciones/publicar-resultados/${this.idConvSeleccionada}`, {}).subscribe({
                next: (res) => {
                    this.publicandoResultados = false;
                    this.showSuccess(`✅ Resultados publicados. Se notificó a ${res.notificados} estudiante(s).`);
                },
                error: (err) => {
                    this.publicandoResultados = false;
                    this.showError('Error al publicar resultados: ' + (err.error || err.message));
                }
            })
        );
    }

    // ─── Tab 4: Actas ────────────────────────────────────────────────────────
    seleccionarPostulanteActas(p: any): void {
        this.postulanteActas = p;
        this.cargarActas(p.idPostulacion);
    }

    cargarActas(idPostulacion: number): void {
        this.subs.add(
            this.http.get<any[]>(`${this.BASE}/evaluaciones/actas/${idPostulacion}`).subscribe({
                next: (actas) => {
                    this.actasMeritos = actas.find(a => a.tipoActa === 'MERITOS') || null;
                    this.actasOposicion = actas.find(a => a.tipoActa === 'OPOSICION') || null;
                },
                error: () => { }
            })
        );
    }

    generarActa(tipo: 'MERITOS' | 'OPOSICION'): void {
        if (!this.postulanteActas) return;
        this.generandoActa = true;
        const body = { idPostulacion: this.postulanteActas.idPostulacion, tipoActa: tipo };
        this.subs.add(
            this.http.post<any>(`${this.BASE}/evaluaciones/actas/generar`, body).subscribe({
                next: () => {
                    this.generandoActa = false;
                    this.showSuccess(`Acta de ${tipo === 'MERITOS' ? 'Méritos' : 'Oposición'} generada.`);
                    this.cargarActas(this.postulanteActas!.idPostulacion);
                },
                error: (err) => { this.generandoActa = false; this.showError('Error: ' + (err.error || err.message)); }
            })
        );
    }

    confirmarActa(acta: any): void {
        if (!acta) return;
        this.confirmandoActa = true;
        const body = { idActa: acta.idActa, idEvaluador: this.userId, rolEvaluador: this.userRol };
        this.subs.add(
            this.http.post<any>(`${this.BASE}/evaluaciones/actas/confirmar`, body).subscribe({
                next: () => {
                    this.confirmandoActa = false;
                    this.showSuccess('Confirmación registrada correctamente.');
                    this.cargarActas(this.postulanteActas!.idPostulacion);
                },
                error: (err) => { this.confirmandoActa = false; this.showError('Error: ' + (err.error || err.message)); }
            })
        );
    }

    yaConfirme(acta: any): boolean {
        if (!acta) return false;
        if (this.userRol === 'DECANO') return acta.confirmadoDecano;
        if (this.userRol === 'COORDINADOR') return acta.confirmadoCoordinador;
        if (this.userRol === 'DOCENTE') return acta.confirmadoDocente;
        return false;
    }

    // ─── Tabs ────────────────────────────────────────────────────────────────
    setTab(tab: Tab): void { this.activeTab = tab; }

    isTabDisponible(tab: Tab): boolean {
        if (tab === 'meritos') return !!this.idConvSeleccionada;
        if (tab === 'oposicion') return this.postulantes.length > 0;
        if (tab === 'resultados') return this.postulantes.length > 0;
        if (tab === 'actas') return this.ranking.length > 0;
        return false;
    }

    // ─── Mensajes ────────────────────────────────────────────────────────────
    private showSuccess(msg: string): void {
        this.successMsg = msg;
        setTimeout(() => this.successMsg = '', 5000);
    }
    private showError(msg: string): void {
        this.errorMsg = msg;
        setTimeout(() => this.errorMsg = '', 5000);
    }
}
