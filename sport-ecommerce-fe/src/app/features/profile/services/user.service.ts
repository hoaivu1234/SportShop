import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from '../../../core/services/base-http.service';
import { USER_API } from '../../../core/constants/api-path.constant';
import { ApiResponse } from '../../../models/api-response.model';
import { UserResponse } from '../../../core/services/storage/storage.service';

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(BaseHttpService);

  getProfile(): Observable<ApiResponse<UserResponse>> {
    return this.http.get<UserResponse>(USER_API.PROFILE);
  }

  updateProfile(data: UpdateProfileRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<UserResponse>(USER_API.PROFILE, data);
  }

  changePassword(data: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<void>(USER_API.CHANGE_PASSWORD, data);
  }
}
