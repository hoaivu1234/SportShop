import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { StorageService } from '../../../core/services/storage/storage.service';
import { AUTH_API } from '../../../core/constants/api-path.constant';

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

interface LoginApiResponse {
  status: number;
  message: string;
  data: AuthTokens;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(StorageService);
  private readonly router = inject(Router);

  login(email: string, password: string): Observable<LoginApiResponse> {
    return this.http
      .post<LoginApiResponse>(AUTH_API.LOGIN, { email, password })
      .pipe(
        tap(response => {
          if (response.data?.accessToken) {
            this.storage.setAccessToken(response.data.accessToken);
            this.storage.setRefreshToken(response.data.refreshToken);
          }
        }),
      );
  }

  getAccessToken(): string | null {
    return this.storage.getAccessToken();
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  logout(): void {
    this.storage.clearTokens();
    this.router.navigate(['/auth']);
  }
}
