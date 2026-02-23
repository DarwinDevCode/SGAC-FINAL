import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import { environment } from '../../../environments/environment';
import {UsuarioDTO} from '../dto/usuario';

export interface LoginRequest {
  usuario: string;
  password: string;
}

export interface TipoRolAuth {
  idTipoRol: number;
  nombreTipoRol: string;
  activo: boolean;
}

export interface AuthUser {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  correo: string;
  nombreUsuario: string;
  rolActual: string;
  roles: TipoRolAuth[];
  activo: boolean;
  token?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private currentUserSubject = new BehaviorSubject<UsuarioDTO | null>(null);

  private readonly baseUrl = (environment as any).apiUrl || 'http://localhost:8080/api';
  private readonly API_AUTH = `${this.baseUrl}/auth`;

  private readonly TOKEN_KEY = 'token';
  private readonly USER_KEY = 'user';

  login(credentials: LoginRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.API_AUTH}/login`, credentials).pipe(
      tap((res) => {
        try {
          if (res?.token) {
            localStorage.setItem(this.TOKEN_KEY, res.token);
          }
          localStorage.setItem(this.USER_KEY, JSON.stringify(res));
        } catch (error) {
          console.error('El navegador bloqueó el acceso al localStorage. Verifica si estás en modo incógnito.', error);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUser(): AuthUser | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) as AuthUser : null;
  }

  getCurrentUser(): UsuarioDTO | null {
    return this.currentUserSubject.value;
  }

  hasRole(roles: string[]): boolean {
    const user = this.getCurrentUser();
    if (!user || !user.rolActual)
      return false;
    return roles.includes(user.rolActual);
  }
}
