import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from '../../../../core/services/base-http.service';
import { ApiResponse } from '../../../../models/api-response.model';
import { PageApiResponse } from '../../../../models/page-response.model';
import { PRODUCT_API } from '../../../../core/constants/api-path.constant';
import {
  ImageResponse,
  ProductDetailResponse,
  ProductImageRequest,
  ProductListParams,
  ProductListResponse,
  ProductRequest,
  VariantResponse,
  ProductVariantRequest,
} from '../../../../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(BaseHttpService);

  // ── Product CRUD ──────────────────────────────────────────────────────────

  getProducts(params: ProductListParams): Observable<PageApiResponse<ProductListResponse>> {
    return this.http.getPaged<ProductListResponse>(
      PRODUCT_API.BASE,
      { page: params.page, size: params.size, sort: params.sort, direction: params.direction },
      {
        keyword: params.keyword,
        categoryId: params.categoryId,
        status: params.status,
        brand: params.brand,
        minPrice: params.minPrice,
        maxPrice: params.maxPrice,
      }
    );
  }

  getProductById(id: number): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.get<ProductDetailResponse>(PRODUCT_API.BY_ID(id));
  }

  /** Creates the product + its images and variants in a single request */
  createProduct(request: ProductRequest): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.post<ProductDetailResponse>(PRODUCT_API.BASE, request);
  }

  /** Updates the product + replaces its images and variants in a single request */
  updateProduct(id: number, request: ProductRequest): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.put<ProductDetailResponse>(PRODUCT_API.BY_ID(id), request);
  }

  deleteProduct(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(PRODUCT_API.BY_ID(id));
  }

  // ── Granular image management (for fine-grained edits after creation) ──────

  createImage(productId: number, request: ProductImageRequest): Observable<ApiResponse<ImageResponse>> {
    return this.http.post<ImageResponse>(`${PRODUCT_API.BASE}/${productId}/images`, request);
  }

  deleteImage(productId: number, imageId: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(`${PRODUCT_API.BASE}/${productId}/images/${imageId}`);
  }

  setMainImage(productId: number, imageId: number): Observable<ApiResponse<ImageResponse>> {
    return this.http.patch<ImageResponse>(`${PRODUCT_API.BASE}/${productId}/images/${imageId}/main`, {});
  }

  // ── Granular variant management ───────────────────────────────────────────

  createVariant(productId: number, request: ProductVariantRequest): Observable<ApiResponse<VariantResponse>> {
    return this.http.post<VariantResponse>(`${PRODUCT_API.BASE}/${productId}/variants`, request);
  }

  updateVariant(productId: number, variantId: number, request: ProductVariantRequest): Observable<ApiResponse<VariantResponse>> {
    return this.http.put<VariantResponse>(`${PRODUCT_API.BASE}/${productId}/variants/${variantId}`, request);
  }

  deleteVariant(productId: number, variantId: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(`${PRODUCT_API.BASE}/${productId}/variants/${variantId}`);
  }
}
