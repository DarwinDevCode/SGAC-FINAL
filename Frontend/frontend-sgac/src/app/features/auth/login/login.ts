import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService, AuthUser, LoginRequest } from '../../../core/services/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})

export class LoginComponent {
  view: 'login' | 'forgot-password' = 'login';
  showPassword = false;
  loading = false;

  identifier = '';
  password = '';

  private authService = inject(AuthService);
  private router = inject(Router);

  handleLogin() {
    if (!this.identifier || !this.password) {
      alert("Por favor ingrese sus credenciales");
      return;
    }

    this.loading = true;

    const payload: LoginRequest = {
      usuario: this.identifier,
      password: this.password
    };

    this.authService.login(payload).subscribe({
      next: (res: AuthUser) => {
        const role = res.rolActual;
        this.redirectByRole(role);
        console.log(role || "Sin rol asignado");
      },
      error: (err: any) => {
        this.loading = false;
        const msg = err.error?.mensaje || "Credenciales incorrectas o servidor no disponible";
        alert(msg);
      }
    });
  }

  handleForgotPassword() {
    alert("Instrucciones enviadas. Si la cuenta existe, recibirá un correo pronto.");
    this.view = 'login';
  }

  private redirectByRole(role: string) {
    const roleRoutes: Record<string, string> = {
      'ADMINISTRADOR': '/admin/dashboard',
      'ESTUDIANTE': '/postulante/dashboard',
      'DOCENTE': '/docente/dashboard',
      'COORDINADOR': '/coordinador/dashboard',
      'DECANO': '/decano/dashboard',
      'AYUDANTE_CATEDRA': '/ayudante/dashboard'
    };

    //console.log(this.authService.getUser())

    const target = roleRoutes[role];
    if (target) {
      this.router.navigate([target]);
    } else {
      alert("Rol no reconocido: " + role);
      this.loading = false;
    }
  }
}
