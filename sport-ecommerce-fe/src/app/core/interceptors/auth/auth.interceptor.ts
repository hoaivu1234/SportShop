import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { StorageService } from '../../services/storage/storage.service';
import {
  AUTH_HEADER,
  NO_CREDENTIALS_URLS,
  PUBLIC_URLS,
  TOKEN_PREFIX,
} from '../../constants/auth.constant';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../../../features/auth/services/auth.service';
import { HTTP_STATUS } from '../../constants/app.constant';

/**
 * - Always adds withCredentials so HttpOnly cookies (OAuth2 users) are sent.
 * - Adds Authorization header for email/password users (token in localStorage).
 * - On 401: tries body-based refresh (email/password) or cookie-based refresh (OAuth2).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storageService = inject(StorageService);
  const authService = inject(AuthService);

  const skipCredentials = NO_CREDENTIALS_URLS.some(url => req.url.includes(url));

  if (skipCredentials) {
    return next(req);
  }
  
  const isPublic = PUBLIC_URLS.some((url) => req.url.includes(url));

  let authReq = req.clone({ withCredentials: true });

  if (isPublic) return next(authReq);

  // Also add Authorization header if token exists in localStorage (email/password login)
  const token = storageService.getAccessToken();
  if (token) {
    authReq = authReq.clone({
      headers: authReq.headers.set(AUTH_HEADER, `${TOKEN_PREFIX} ${token}`),
    });
  }

  return next(authReq).pipe(
    catchError((err) => {
      if (err.status === HTTP_STATUS.UNAUTHORIZED) {
        const refreshToken = storageService.getRefreshToken();

        if (refreshToken) {
          // Email/password user: refresh via request body
          return authService.refreshToken(refreshToken).pipe(
            switchMap((res) => {
              const newToken = res.data.accessToken;
              const retryReq = req.clone({
                withCredentials: true,
                headers: req.headers.set(AUTH_HEADER, `${TOKEN_PREFIX} ${newToken}`),
              });
              return next(retryReq);
            }),
            catchError(() => {
              authService.logout();
              return throwError(() => err);
            }),
          );
        } else {
          // OAuth2 user: refresh via HttpOnly cookie — backend rotates cookie
          return authService.refreshTokenFromCookie().pipe(
            switchMap(() => next(req.clone({ withCredentials: true }))),
            catchError(() => {
              authService.logout();
              return throwError(() => err);
            }),
          );
        }
      }
      return throwError(() => err);
    }),
  );
};
