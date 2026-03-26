export interface WishlistItemResponse {
  id: number;
  productId: number;
  variantId: number | null;
  productName: string;
  productSlug: string;
  mainImageUrl: string | null;
  price: number;
  discountPrice: number | null;
  totalStock: number;
  createdAt: string;
}

export interface AddToWishlistRequest {
  productId: number;
  variantId?: number | null;
}
