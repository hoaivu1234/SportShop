import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthUtil } from '../../services/auth/auth.util';
import { AUTH_ROUTES } from '../../constants/auth.constant';

/**
 * Functional guard — protects routes that require authentication.
 * Redirects to /auth/login with a returnUrl query param on failure.
 *
 * Usage in routes:
 *   { path: 'profile', canActivate: [authGuard], component: ProfileComponent }
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const router = inject(Router);

  if (AuthUtil.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree([AUTH_ROUTES.LOGIN], {
    queryParams: { returnUrl: state.url },
  });
};
