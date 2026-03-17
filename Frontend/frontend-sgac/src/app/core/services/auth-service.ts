import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  usuario: string;
  password: string;
}

export interface TipoRolAuth {
  idTipoRol:     number;
  nombreTipoRol: string;
  activo:        boolean;
}

export interface AuthUser {
  idUsuario:    number;
  nombres:      string;
  apellidos:    string;
  correo:       string;
  nombreUsuario: string;
  rolActual:    string | null;
  roles:        TipoRolAuth[];
  activo:       boolean;
  token?:       string;
}

export interface SeleccionarRolRequest {
  preAuthToken:   string;
  rolSeleccionado: string;
}

export interface PreAuthData {
  preAuthToken: string;
  roles:        TipoRolAuth[];
  nombres:      string;
  apellidos:    string | null;
  correo:       string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private http   = inject(HttpClient);
  private router = inject(Router);

  private readonly baseUrl   = environment.apiUrl;
  private readonly API_AUTH  = `${this.baseUrl}/auth`;

  private readonly TOKEN_KEY   = 'token';
  private readonly USER_KEY    = 'user';
  private readonly PREAUTH_KEY = 'sgac_preauth';  // temporal, solo durante selección

  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.getUser());

  login(credentials: LoginRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.API_AUTH}/login`, credentials).pipe(
      tap((res) => {
        const preAuthData: PreAuthData = {
          preAuthToken: res.token!,
          roles:        res.roles,
          nombres:      res.nombres,
          apellidos:    res.apellidos,
          correo:       res.correo,
        };
        localStorage.setItem(this.PREAUTH_KEY, JSON.stringify(preAuthData));
      })
    );
  }

  seleccionarRol(rolSeleccionado: string): Observable<AuthUser> {
    const preAuthData = this.getPreAuthData();
    if (!preAuthData) {
      throw new Error('No hay datos de pre-autenticación. Inicia sesión nuevamente.');
    }

    const request: SeleccionarRolRequest = {
      preAuthToken:    preAuthData.preAuthToken,
      rolSeleccionado: rolSeleccionado,
    };

    return this.http.post<AuthUser>(`${this.API_AUTH}/seleccionar-rol`, request).pipe(
      tap((res) => {
        // Ahora sí guardamos el token definitivo y los datos del usuario
        if (res.token) {
          localStorage.setItem(this.TOKEN_KEY, res.token);
        }
        localStorage.setItem(this.USER_KEY, JSON.stringify(res));
        // Limpiar el pre-auth temporal — ya no es necesario
        localStorage.removeItem(this.PREAUTH_KEY);
        this.currentUserSubject.next(res);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.PREAUTH_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUser(): AuthUser | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) as AuthUser : null;
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  getPreAuthData(): PreAuthData | null {
    const raw = localStorage.getItem(this.PREAUTH_KEY);
    return raw ? JSON.parse(raw) as PreAuthData : null;
  }

  hasRole(roles: string[]): boolean {
    const user = this.getUser();
    if (!user?.rolActual) return false;
    return roles.includes(user.rolActual);
  }

  getRolActivo(): string | null {
    return this.getUser()?.rolActual ?? null;
  }
}
