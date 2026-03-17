import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { loadingInterceptor } from './core/interceptors/loading/loading.interceptor';
import { authInterceptor } from './core/interceptors/auth/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([loadingInterceptor, authInterceptor, errorInterceptor]),
    ),
  ],
};
