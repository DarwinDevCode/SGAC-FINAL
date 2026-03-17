import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router      = inject(Router);
  const token    = authService.getToken();
  const rolActivo = authService.getRolActivo();
  let clonedReq = req;

  if (token) {
    const headers: Record<string, string> = {
      Authorization: `Bearer ${token}`,
    };

    if (rolActivo)
      headers['X-Active-Role'] = rolActivo;

    clonedReq = req.clone({ setHeaders: headers });
  }

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if ((error.status === 401 || error.status === 403) && token) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
