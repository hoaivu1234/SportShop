import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { BaseHttpService } from '../../../core/services/base-http.service';
import { COUPON_API } from '../../../core/constants/api-path.constant';
import { ApiResponse } from '../../../models/api-response.model';
import {
  ApplyCouponRequest,
  ApplyCouponResponse,
  CouponResponse,
} from '../../../models/coupon.model';

@Injectable({ providedIn: 'root' })
export class CouponService {
  private readonly http = inject(BaseHttpService);

  applyCoupon(request: ApplyCouponRequest): Observable<ApiResponse<ApplyCouponResponse>> {
    return this.http.post<ApplyCouponResponse>(COUPON_API.APPLY, request);
  }

  getCouponByCode(code: string): Observable<ApiResponse<CouponResponse>> {
    return this.http.get<CouponResponse>(COUPON_API.BY_CODE(code));
  }
}
