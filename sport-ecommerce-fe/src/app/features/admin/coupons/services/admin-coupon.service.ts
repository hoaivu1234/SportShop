import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { BaseHttpService } from '../../../../core/services/base-http.service';
import { COUPON_API } from '../../../../core/constants/api-path.constant';
import { ApiResponse } from '../../../../models/api-response.model';
import { PageApiResponse } from '../../../../models/page-response.model';
import {
  CouponResponse,
  CreateCouponRequest,
  UpdateCouponRequest,
} from '../../../../models/coupon.model';

@Injectable({ providedIn: 'root' })
export class AdminCouponService {
  private readonly http = inject(BaseHttpService);

  getCoupons(page = 0, size = 20): Observable<PageApiResponse<CouponResponse>> {
    return this.http.getPaged<CouponResponse>(COUPON_API.ADMIN_BASE, { page, size, sort: 'createdAt', direction: 'desc' });
  }

  createCoupon(request: CreateCouponRequest): Observable<ApiResponse<CouponResponse>> {
    return this.http.post<CouponResponse>(COUPON_API.ADMIN_BASE, request);
  }

  updateCoupon(id: number, request: UpdateCouponRequest): Observable<ApiResponse<CouponResponse>> {
    return this.http.put<CouponResponse>(COUPON_API.ADMIN_BY_ID(id), request);
  }

  deleteCoupon(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(COUPON_API.ADMIN_BY_ID(id));
  }
}
