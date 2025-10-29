import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from './token.service';

export const authGuard: CanActivateFn = () => {
  const tokenSvc = inject(TokenService);
  const router = inject(Router);
  if (tokenSvc.isLoggedIn()) return true;
  router.navigate(['/login']);
  return false;
};
