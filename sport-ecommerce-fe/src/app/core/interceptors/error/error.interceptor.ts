import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../../services/toast.service';
import { AuthService } from '../../../features/auth/services/auth.service';
import { HTTP_STATUS, ERROR_MESSAGES } from '../../constants/app.constant';
import { AUTH_ROUTES } from '../../constants/auth.constant';
import { ApiResponse } from '../../../models/api-response.model';

/**
 * Global HTTP error handler:
 * – 401 → clears tokens and redirects to login
 * – 403 → redirects to /forbidden
 * – all others → shows a toast with the resolved message
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const serverMessage =
        (error.error as ApiResponse<unknown>)?.message ??
        ERROR_MESSAGES[error.status] ??
        'Đã xảy ra lỗi. Vui lòng thử lại.';

      switch (error.status) {
        case HTTP_STATUS.UNAUTHORIZED:
          authService.logout();
          router.navigate([AUTH_ROUTES.LOGIN], {
            queryParams: { returnUrl: router.url },
          });
          break;

        case HTTP_STATUS.FORBIDDEN:
          toastService.error(serverMessage);
          router.navigate([AUTH_ROUTES.FORBIDDEN]);
          break;

        case HTTP_STATUS.INTERNAL_SERVER_ERROR:
        case HTTP_STATUS.BAD_GATEWAY:
        case HTTP_STATUS.SERVICE_UNAVAILABLE:
          toastService.error(serverMessage, 'Lỗi máy chủ');
          break;

        default:
          // 400, 404, 409, 422 — let components decide whether to show a toast
          break;
      }

      return throwError(() => error);
    }),
  );
};
