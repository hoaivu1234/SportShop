export interface ReviewResponse {
  id: number;
  productId: number;
  userId: number;
  userFullName: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface ReviewSummaryResponse {
  averageRating: number;
  totalReviews: number;
  /** Key = star value (1–5), value = count of reviews at that rating. */
  ratingDistribution: Record<number, number>;
}

export interface CreateReviewRequest {
  productId: number;
  rating: number;
  comment: string;
}

export interface UpdateReviewRequest {
  rating: number;
  comment: string;
}
