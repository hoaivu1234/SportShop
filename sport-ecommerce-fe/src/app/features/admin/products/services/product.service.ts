import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from '../../../../core/services/base-http.service';
import { ApiResponse } from '../../../../models/api-response.model';
import { environment } from '../../../../../environments/environment';
import {
  ImageResponse,
  ProductDetailResponse,
  ProductImageRequest,
  ProductRequest,
  ProductVariantRequest,
  VariantResponse,
} from '../../../../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly base = inject(BaseHttpService);
  private readonly apiUrl = `${environment.apiUrl}/api/v1/products`;

  createProduct(request: ProductRequest): Observable<ApiResponse<ProductDetailResponse>> {
    return this.base.post<ProductDetailResponse>(this.apiUrl, request);
  }

  createImage(
    productId: number,
    request: ProductImageRequest
  ): Observable<ApiResponse<ImageResponse>> {
    return this.base.post<ImageResponse>(`${this.apiUrl}/${productId}/images`, request);
  }

  createVariant(
    productId: number,
    request: ProductVariantRequest
  ): Observable<ApiResponse<VariantResponse>> {
    return this.base.post<VariantResponse>(`${this.apiUrl}/${productId}/variants`, request);
  }
}
