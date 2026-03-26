import { Injectable, DestroyRef, inject, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CouponService } from './coupon.service';
import { ApplyCouponResponse } from '../../../models/coupon.model';

/**
 * CouponStateService — manages applied coupon state for the cart session.
 *
 * Consumed by CartSummaryComponent to show discount and update the total.
 * Deliberately decoupled from CartStateService: the coupon is a display
 * concern (how much the user saves) and does not mutate the server-side cart.
 */
@Injectable({ providedIn: 'root' })
export class CouponStateService {
  private readonly couponSvc  = inject(CouponService);
  private readonly destroyRef = inject(DestroyRef);

  // ── State ──────────────────────────────────────────────────────────────────
  readonly applied    = signal<ApplyCouponResponse | null>(null);
  readonly isApplying = signal(false);
  readonly error      = signal<string | null>(null);

  readonly discountAmount = computed(() => this.applied()?.discountAmount ?? 0);
  readonly isApplied      = computed(() => this.applied() !== null);

  // ── Commands ───────────────────────────────────────────────────────────────

  apply(code: string, cartTotal: number): void {
    if (!code.trim()) {
      this.error.set('Please enter a coupon code');
      return;
    }
    if (this.isApplying()) return;

    this.isApplying.set(true);
    this.error.set(null);

    this.couponSvc.applyCoupon({ code: code.trim().toUpperCase(), cartTotal })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.applied.set(res.data);
          this.isApplying.set(false);
        },
        error: err => {
          this.error.set(err?.error?.resolvedMessage ?? 'Invalid coupon code');
          this.isApplying.set(false);
        },
      });
  }

  remove(): void {
    this.applied.set(null);
    this.error.set(null);
  }
}
