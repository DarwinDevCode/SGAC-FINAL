import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../services/auth-service';
import {inject} from '@angular/core';

export const selectorRolGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const preAuthData = authService.getPreAuthData();

  if (preAuthData && preAuthData.preAuthToken) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
