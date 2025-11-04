import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from './token.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const tokens = inject(TokenService);
  if (tokens.isUnauthenticated()) {
    router.navigate(['/login']);
    return false;
  }
  return true;
};
