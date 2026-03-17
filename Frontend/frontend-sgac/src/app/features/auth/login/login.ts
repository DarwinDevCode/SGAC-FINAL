import { Component, inject } from '@angular/core';
import { CommonModule }     from '@angular/common';
import { FormsModule }      from '@angular/forms';
import { Router }           from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService, AuthUser, LoginRequest } from '../../../core/services/auth-service';

@Component({
  selector:    'app-login',
  standalone:  true,
  imports:     [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './login.html',
  styleUrls:   ['./login.css']
})
export class LoginComponent {

  view: 'login' | 'forgot-password' = 'login';
  showPassword = false;
  loading      = false;
  errorMsg     = '';

  identifier = '';
  password   = '';

  private authService = inject(AuthService);
  private router      = inject(Router);

  handleLogin(): void {
    if (!this.identifier || !this.password) {
      this.errorMsg = 'Por favor ingrese sus credenciales.';
      return;
    }

    this.loading  = true;
    this.errorMsg = '';

    const payload: LoginRequest = {
      usuario:  this.identifier,
      password: this.password,
    };

    this.authService.login(payload).subscribe({
      next: (res: AuthUser) => {
        this.loading = false;
        this.router.navigate(['/seleccionar-rol']);
      },
      error: (err: any) => {
        this.loading  = false;
        this.errorMsg = err.error?.message
          ?? err.error?.mensaje
          ?? 'Credenciales incorrectas o servidor no disponible.';
      }
    });
  }

  handleForgotPassword(): void {
    // TODO: implementar recuperación de contraseña
    alert('Instrucciones enviadas. Si la cuenta existe, recibirá un correo pronto.');
    this.view = 'login';
  }
}
