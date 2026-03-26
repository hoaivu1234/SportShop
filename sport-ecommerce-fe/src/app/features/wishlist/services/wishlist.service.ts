import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { BaseHttpService } from '../../../core/services/base-http.service';
import { WISHLIST_API } from '../../../core/constants/api-path.constant';
import { ApiResponse } from '../../../models/api-response.model';
import { AddToWishlistRequest, WishlistItemResponse } from '../../../models/wishlist.model';

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private readonly http = inject(BaseHttpService);

  getWishlist(): Observable<ApiResponse<WishlistItemResponse[]>> {
    return this.http.get<WishlistItemResponse[]>(WISHLIST_API.BASE);
  }

  addItem(request: AddToWishlistRequest): Observable<ApiResponse<WishlistItemResponse>> {
    return this.http.post<WishlistItemResponse>(WISHLIST_API.BASE, request);
  }

  removeItem(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(WISHLIST_API.BY_ID(id));
  }

  clearWishlist(): Observable<ApiResponse<void>> {
    return this.http.delete<void>(WISHLIST_API.BASE);
  }
}
