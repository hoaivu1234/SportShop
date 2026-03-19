import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { StorageService, UserResponse } from '../../../core/services/storage/storage.service';
import { AUTH_API } from '../../../core/constants/api-path.constant';
import { DatePipe } from '@angular/common';

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
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

  private readonly _loggedIn = signal<boolean>(!!this.storage.getAccessToken());
  readonly loggedIn = this._loggedIn.asReadonly();

  login(email: string, password: string): Observable<LoginApiResponse> {
    return this.http
      .post<LoginApiResponse>(AUTH_API.LOGIN, { email, password })
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  register(data: any): Observable<LoginApiResponse> {
    return this.http
      .post<LoginApiResponse>(AUTH_API.REGISTER, data)
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  private handleAuthResponse(response: LoginApiResponse) {
    if (response.data?.accessToken) {
      this.storage.setAccessToken(response.data.accessToken);
      this.storage.setRefreshToken(response.data.refreshToken);
      this.storage.setUserInfo(response.data.user);
      this._loggedIn.set(true);
    }
  }

  getAccessToken(): string | null {
    return this.storage.getAccessToken();
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  logout(): void {
    this.storage.clearTokens();
    this._loggedIn.set(false);
  }
}
