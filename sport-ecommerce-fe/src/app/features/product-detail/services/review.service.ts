import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { BaseHttpService } from '../../../core/services/base-http.service';
import { REVIEW_API } from '../../../core/constants/api-path.constant';
import { ApiResponse } from '../../../models/api-response.model';
import { PageApiResponse } from '../../../models/page-response.model';
import {
  ReviewResponse,
  ReviewSummaryResponse,
  CreateReviewRequest,
  UpdateReviewRequest,
} from '../../../models/review.model';
import { SortDirection } from '../../../models/pagination.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly http = inject(BaseHttpService);

  getProductReviews(
    productId: number,
    page = 0,
    size = 10,
    sort = 'createdAt',
    direction: SortDirection = 'desc',
  ): Observable<PageApiResponse<ReviewResponse>> {
    return this.http.getPaged<ReviewResponse>(
      REVIEW_API.BY_PRODUCT(productId),
      { page, size, sort, direction },
    );
  }

  getProductSummary(productId: number): Observable<ApiResponse<ReviewSummaryResponse>> {
    return this.http.get<ReviewSummaryResponse>(REVIEW_API.SUMMARY(productId));
  }

  createReview(request: CreateReviewRequest): Observable<ApiResponse<ReviewResponse>> {
    return this.http.post<ReviewResponse>(REVIEW_API.BASE, request);
  }

  updateReview(id: number, request: UpdateReviewRequest): Observable<ApiResponse<ReviewResponse>> {
    return this.http.put<ReviewResponse>(REVIEW_API.BY_ID(id), request);
  }

  deleteReview(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(REVIEW_API.BY_ID(id));
  }

  getMyReviews(page = 0, size = 10): Observable<PageApiResponse<ReviewResponse>> {
    return this.http.getPaged<ReviewResponse>(REVIEW_API.MY, { page, size });
  }
}
