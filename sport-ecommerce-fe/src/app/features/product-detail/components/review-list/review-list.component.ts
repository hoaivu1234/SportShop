import {
  Component,
  DestroyRef,
  Input,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../../auth/services/auth.service';
import { StorageService } from '../../../../core/services/storage/storage.service';
import { ToastService } from '../../../../core/services/toast.service';
import {
  ReviewResponse,
  ReviewSummaryResponse,
} from '../../../../models/review.model';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css',
})
export class ReviewListComponent implements OnInit {
  @Input({ required: true }) productId!: number;

  private readonly reviewSvc  = inject(ReviewService);
  private readonly authSvc    = inject(AuthService);
  private readonly storageSvc = inject(StorageService);
  private readonly toast      = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  // ── Auth ──────────────────────────────────────────────────────────────────
  readonly isLoggedIn = this.authSvc.loggedIn;

  get currentUserId(): number | null {
    return this.storageSvc.getUserInfo()?.id ?? null;
  }

  // ── Review data ───────────────────────────────────────────────────────────
  reviews       = signal<ReviewResponse[]>([]);
  summary       = signal<ReviewSummaryResponse | null>(null);
  isLoadingList = signal(true);
  isLoadingSummary = signal(true);
  error         = signal<string | null>(null);

  page       = signal(0);
  totalPages = signal(0);
  readonly pageSize = 5;

  readonly skeletonCards = [1, 2, 3];

  // ── Form state ────────────────────────────────────────────────────────────
  formMode    = signal<'none' | 'create' | 'edit'>('none');
  formRating  = signal(0);
  hoveredStar = signal(0);
  formComment = signal('');
  editingId   = signal<number | null>(null);
  isSubmitting = signal(false);

  // ── Delete state ─────────────────────────────────────────────────────────
  deletingId = signal<number | null>(null);

  // ── Computed helpers ──────────────────────────────────────────────────────
  get myReview(): ReviewResponse | null {
    const uid = this.currentUserId;
    if (!uid) return null;
    return this.reviews().find(r => r.userId === uid) ?? null;
  }

  get ratingBreakdown(): { stars: number; count: number; percent: number }[] {
    const dist  = this.summary()?.ratingDistribution ?? {};
    const total = this.summary()?.totalReviews ?? 0;
    return [5, 4, 3, 2, 1].map(stars => {
      const count = dist[stars] ?? 0;
      return { stars, count, percent: total > 0 ? Math.round((count / total) * 100) : 0 };
    });
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.loadSummary();
    this.loadReviews();
  }

  // ── Data loading ──────────────────────────────────────────────────────────
  loadSummary(): void {
    this.isLoadingSummary.set(true);
    this.reviewSvc.getProductSummary(this.productId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next:  res => { this.summary.set(res.data); this.isLoadingSummary.set(false); },
        error: ()  => this.isLoadingSummary.set(false),
      });
  }

  loadReviews(): void {
    this.isLoadingList.set(true);
    this.error.set(null);
    this.reviewSvc.getProductReviews(this.productId, this.page(), this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.reviews.set(res.data.content);
          this.totalPages.set(res.data.totalPages);
          this.isLoadingList.set(false);
        },
        error: () => {
          this.error.set('Failed to load reviews.');
          this.isLoadingList.set(false);
        },
      });
  }

  goToPage(p: number): void {
    this.page.set(p);
    this.loadReviews();
  }

  // ── Form actions ──────────────────────────────────────────────────────────
  openCreateForm(): void {
    this.formRating.set(0);
    this.formComment.set('');
    this.editingId.set(null);
    this.formMode.set('create');
  }

  openEditForm(review: ReviewResponse): void {
    this.formRating.set(review.rating);
    this.formComment.set(review.comment);
    this.editingId.set(review.id);
    this.formMode.set('edit');
  }

  cancelForm(): void {
    this.formMode.set('none');
    this.hoveredStar.set(0);
  }

  setRating(star: number): void {
    this.formRating.set(star);
  }

  setHoveredStar(star: number): void {
    this.hoveredStar.set(star);
  }

  clearHoveredStar(): void {
    this.hoveredStar.set(0);
  }

  getEffectiveStar(star: number): boolean {
    const effective = this.hoveredStar() || this.formRating();
    return star <= effective;
  }

  submitForm(): void {
    if (this.formRating() === 0) {
      this.toast.warning('Please select a rating');
      return;
    }
    if (!this.formComment().trim()) {
      this.toast.warning('Please write a comment');
      return;
    }
    if (this.isSubmitting()) return;

    this.isSubmitting.set(true);

    if (this.formMode() === 'create') {
      this.reviewSvc.createReview({
        productId: this.productId,
        rating:    this.formRating(),
        comment:   this.formComment().trim(),
      }).pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: res => {
            this.reviews.update(list => [res.data, ...list]);
            this.formMode.set('none');
            this.isSubmitting.set(false);
            this.toast.success('Review submitted!');
            this.loadSummary();
          },
          error: err => {
            this.isSubmitting.set(false);
            this.toast.error(err?.error?.resolvedMessage ?? 'Failed to submit review');
          },
        });
    } else {
      const id = this.editingId()!;
      this.reviewSvc.updateReview(id, {
        rating:  this.formRating(),
        comment: this.formComment().trim(),
      }).pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: res => {
            this.reviews.update(list =>
              list.map(r => r.id === id ? res.data : r));
            this.formMode.set('none');
            this.isSubmitting.set(false);
            this.toast.success('Review updated!');
            this.loadSummary();
          },
          error: err => {
            this.isSubmitting.set(false);
            this.toast.error(err?.error?.resolvedMessage ?? 'Failed to update review');
          },
        });
    }
  }

  // ── Delete ────────────────────────────────────────────────────────────────
  deleteReview(id: number): void {
    if (!confirm('Delete this review?')) return;
    this.deletingId.set(id);
    this.reviewSvc.deleteReview(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.reviews.update(list => list.filter(r => r.id !== id));
          this.deletingId.set(null);
          this.toast.success('Review deleted');
          this.loadSummary();
        },
        error: err => {
          this.deletingId.set(null);
          this.toast.error(err?.error?.resolvedMessage ?? 'Failed to delete review');
        },
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  getStars(rating: number): ('full' | 'half' | 'empty')[] {
    const result: ('full' | 'half' | 'empty')[] = [];
    for (let i = 1; i <= 5; i++) {
      if (rating >= i)           result.push('full');
      else if (rating >= i - 0.5) result.push('half');
      else                        result.push('empty');
    }
    return result;
  }

  getInitials(name: string): string {
    const parts = name.trim().split(' ').filter(p => p.length > 0);
    if (parts.length === 0) return '?';
    if (parts.length === 1) return parts[0][0].toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }
}
