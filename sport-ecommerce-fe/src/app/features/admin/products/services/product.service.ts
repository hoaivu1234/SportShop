import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from '../../../../core/services/base-http.service';
import { ApiResponse } from '../../../../models/api-response.model';
import { PageApiResponse } from '../../../../models/page-response.model';
import { ADMIN_PRODUCT_API, PUBLIC_PRODUCT_API } from '../../../../core/constants/api-path.constant';
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

  // ── Public read (no auth) ─────────────────────────────────────────────────

  /** Returns ACTIVE products only — for storefront and homepage use. */
  getProducts(params: ProductListParams): Observable<PageApiResponse<ProductListResponse>> {
    return this.http.getPaged<ProductListResponse>(
      PUBLIC_PRODUCT_API.BASE,
      { page: params.page, size: params.size },
      {
        keyword:      params.keyword,
        categoryId:   params.categoryId,
        categorySlug: params.categorySlug,
        brand:        params.brand,
        minPrice:     params.minPrice,
        maxPrice:     params.maxPrice,
        onSale:       params.onSale,
        // Backend expects sortBy/sortDir as separate @RequestParam, not combined Pageable format
        sortBy:       params.sort      ?? 'createdAt',
        sortDir:      params.direction ?? 'desc',
      }
    );
  }

  getProductById(id: number): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.get<ProductDetailResponse>(PUBLIC_PRODUCT_API.BY_ID(id));
  }

  getProductBySlug(slug: string): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.get<ProductDetailResponse>(PUBLIC_PRODUCT_API.BY_SLUG(slug));
  }

  // ── Admin read (ROLE_ADMIN) ───────────────────────────────────────────────

  /** Returns all products (any status) — for admin product management. */
  getAdminProducts(params: ProductListParams): Observable<PageApiResponse<ProductListResponse>> {
    return this.http.getPaged<ProductListResponse>(
      ADMIN_PRODUCT_API.BASE,
      { page: params.page, size: params.size },
      {
        keyword:    params.keyword,
        categoryId: params.categoryId,
        status:     params.status,
        brand:      params.brand,
        minPrice:   params.minPrice,
        maxPrice:   params.maxPrice,
        sortBy:     params.sort      ?? 'createdAt',
        sortDir:    params.direction ?? 'desc',
      }
    );
  }

  // ── Admin write ───────────────────────────────────────────────────────────

  /** Creates the product + its images and variants in a single request */
  createProduct(request: ProductRequest): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.post<ProductDetailResponse>(ADMIN_PRODUCT_API.BASE, request);
  }

  /** Updates the product + replaces its images and variants in a single request */
  updateProduct(id: number, request: ProductRequest): Observable<ApiResponse<ProductDetailResponse>> {
    return this.http.put<ProductDetailResponse>(ADMIN_PRODUCT_API.BY_ID(id), request);
  }

  deleteProduct(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(ADMIN_PRODUCT_API.BY_ID(id));
  }

  /**
   * Downloads all products matching the given filters as a CSV Blob.
   */
  exportProducts(params: { keyword?: string; categoryId?: number; brand?: string; status?: string }): Observable<Blob> {
    return this.http.getBlob(ADMIN_PRODUCT_API.EXPORT, {
      keyword:    params.keyword,
      categoryId: params.categoryId,
      brand:      params.brand,
      status:     params.status,
    });
  }

  // ── Granular image management ─────────────────────────────────────────────

  createImage(productId: number, request: ProductImageRequest): Observable<ApiResponse<ImageResponse>> {
    return this.http.post<ImageResponse>(ADMIN_PRODUCT_API.IMAGES(productId), request);
  }

  deleteImage(productId: number, imageId: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(ADMIN_PRODUCT_API.IMAGE_BY_ID(productId, imageId));
  }

  setMainImage(productId: number, imageId: number): Observable<ApiResponse<ImageResponse>> {
    return this.http.patch<ImageResponse>(ADMIN_PRODUCT_API.IMAGE_MAIN(productId, imageId), {});
  }

  // ── Granular variant management ───────────────────────────────────────────

  createVariant(productId: number, request: ProductVariantRequest): Observable<ApiResponse<VariantResponse>> {
    return this.http.post<VariantResponse>(ADMIN_PRODUCT_API.VARIANTS(productId), request);
  }

  updateVariant(productId: number, variantId: number, request: ProductVariantRequest): Observable<ApiResponse<VariantResponse>> {
    return this.http.put<VariantResponse>(ADMIN_PRODUCT_API.VARIANT_BY_ID(productId, variantId), request);
  }

  deleteVariant(productId: number, variantId: number): Observable<ApiResponse<void>> {
    return this.http.delete<void>(ADMIN_PRODUCT_API.VARIANT_BY_ID(productId, variantId));
  }
}
