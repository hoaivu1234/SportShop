export type DiscountType  = 'PERCENT' | 'FIXED';
export type CouponStatus  = 'ACTIVE' | 'EXPIRED' | 'DISABLED';

export interface CouponResponse {
  id: number;
  code: string;
  discountType: DiscountType;
  discountValue: number;
  minOrderValue: number | null;
  maxDiscountValue: number | null;
  usageLimit: number | null;
  usedCount: number;
  startDate: string | null;
  endDate: string | null;
  status: CouponStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ApplyCouponRequest {
  code: string;
  cartTotal: number;
}

export interface ApplyCouponResponse {
  couponCode: string;
  discountAmount: number;
  finalAmount: number;
  message: string;
}

export interface CreateCouponRequest {
  code: string;
  discountType: DiscountType;
  discountValue: number;
  minOrderValue?: number | null;
  maxDiscountValue?: number | null;
  usageLimit?: number | null;
  startDate?: string | null;
  endDate?: string | null;
  status?: CouponStatus;
}

export interface UpdateCouponRequest {
  discountType?: DiscountType;
  discountValue?: number;
  minOrderValue?: number | null;
  maxDiscountValue?: number | null;
  usageLimit?: number | null;
  startDate?: string | null;
  endDate?: string | null;
  status?: CouponStatus;
}
