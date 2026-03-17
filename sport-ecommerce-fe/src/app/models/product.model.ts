export interface ProductRequest {
  name: string;
  description?: string;
  brand?: string;
  price: number;
  discountPrice?: number;
  categoryId?: number;
  status: string;
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
