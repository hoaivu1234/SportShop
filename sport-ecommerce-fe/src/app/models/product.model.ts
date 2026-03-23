// ── Requests ─────────────────────────────────────────────────────────────────

import { SortDirection } from "./pagination.model";

export interface ProductRequest {
  name: string;
  description?: string;
  brand?: string;
  price: number;
  discountPrice?: number;
  categoryId?: number;
  status: string;
  /** Images to save alongside the product (URLs already uploaded to Cloudinary) */
  images?: ProductImageRequest[];
  /** Variants to save alongside the product */
  variants?: ProductVariantRequest[];
}

export interface ProductImageRequest {
  imageUrl: string;
  isMain: boolean;
  sortOrder?: number;
}

export interface ProductVariantRequest {
  sku: string;
  size?: string;
  color?: string;
  price: number;
  stock: number;
}

// ── Responses ─────────────────────────────────────────────────────────────────

export interface ImageResponse {
  id: number;
  imageUrl: string;
  isMain: boolean;
  sortOrder?: number;
}

export interface VariantResponse {
  id: number;
  sku: string;
  size?: string;
  color?: string;
  price: number;
  stock: number;
}

export interface CategoryInfo {
  id: number;
  name: string;
  slug: string;
}

export interface ProductDetailResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
  brand?: string;
  price: number;
  discountPrice?: number;
  status: string;
  category?: CategoryInfo;
  images: ImageResponse[];
  variants: VariantResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductListResponse {
  id: number;
  name: string;
  slug: string;
  brand?: string;
  price: number;
  discountPrice?: number;
  status: string;
  mainImageUrl?: string;
  totalStock?: number;
  categoryId?: number;
  categoryName?: string;
  createdAt: string;
}

// ── Params ────────────────────────────────────────────────────────────────────

export interface ProductListParams {
  page: number;
  size: number;
  sort?: string;
  direction?: SortDirection;
  keyword?: string;
  categoryId?: number;
  status?: string;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
}
