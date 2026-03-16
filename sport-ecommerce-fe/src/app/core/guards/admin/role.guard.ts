import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthUtil } from '../../services/auth/auth.util';
import { AUTH_ROUTES } from '../../constants/auth.constant';

/**
 * Functional role guard — protects routes that require specific roles.
 *
 * Usage in routes:
 *   {
 *     path: 'admin',
 *     canActivate: [roleGuard],
 *     data: { roles: ['ROLE_ADMIN'] },
 *     component: AdminComponent
 *   }
 *
 * If no roles are defined in route data, the guard only checks authentication.
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  if (!AuthUtil.isLoggedIn()) {
    return router.createUrlTree([AUTH_ROUTES.LOGIN], {
      queryParams: { returnUrl: state.url },
    });
  }

  const requiredRoles = route.data?.['roles'] as string[] | undefined;

  // No role restriction on this route — just being logged in is enough
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }

  const hasRequiredRole = AuthUtil.hasAnyRole(requiredRoles);

  if (!hasRequiredRole) {
    return router.createUrlTree([AUTH_ROUTES.FORBIDDEN]);
  }

  return true;
};
