import {
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AdminCouponService } from './services/admin-coupon.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  CouponResponse,
  CouponStatus,
  CreateCouponRequest,
  DiscountType,
  UpdateCouponRequest,
} from '../../../models/coupon.model';

interface CouponForm {
  code: string;
  discountType: DiscountType;
  discountValue: number | null;
  minOrderValue: number | null;
  maxDiscountValue: number | null;
  usageLimit: number | null;
  startDate: string;
  endDate: string;
  status: CouponStatus;
}

function emptyForm(): CouponForm {
  return {
    code: '',
    discountType: 'PERCENT',
    discountValue: null,
    minOrderValue: null,
    maxDiscountValue: null,
    usageLimit: null,
    startDate: '',
    endDate: '',
    status: 'ACTIVE',
  };
}

@Component({
  selector: 'app-admin-coupons',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-coupons.component.html',
  styleUrl: './admin-coupons.component.css',
})
export class AdminCouponsComponent implements OnInit {
  private readonly svc        = inject(AdminCouponService);
  private readonly toast      = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  // ── List state ────────────────────────────────────────────────────────────
  coupons    = signal<CouponResponse[]>([]);
  isLoading  = signal(true);
  totalPages = signal(0);
  page       = signal(0);

  // ── Form state ────────────────────────────────────────────────────────────
  showForm    = signal(false);
  editingId   = signal<number | null>(null);
  isSubmitting = signal(false);
  form: CouponForm = emptyForm();

  // ── Delete state ──────────────────────────────────────────────────────────
  deletingId = signal<number | null>(null);

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.svc.getCoupons(this.page(), 20)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.coupons.set(res.data.content);
          this.totalPages.set(res.data.totalPages);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }

  goToPage(p: number): void {
    this.page.set(p);
    this.load();
  }

  // ── Form ──────────────────────────────────────────────────────────────────

  openCreate(): void {
    this.editingId.set(null);
    this.form = emptyForm();
    this.showForm.set(true);
  }

  openEdit(coupon: CouponResponse): void {
    this.editingId.set(coupon.id);
    this.form = {
      code: coupon.code,
      discountType: coupon.discountType,
      discountValue: coupon.discountValue,
      minOrderValue: coupon.minOrderValue,
      maxDiscountValue: coupon.maxDiscountValue,
      usageLimit: coupon.usageLimit,
      startDate: coupon.startDate ? coupon.startDate.substring(0, 16) : '',
      endDate: coupon.endDate ? coupon.endDate.substring(0, 16) : '',
      status: coupon.status,
    };
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.discountValue || this.form.discountValue <= 0) {
      this.toast.warning('Discount value is required');
      return;
    }
    if (this.isSubmitting()) return;
    this.isSubmitting.set(true);

    const id = this.editingId();

    if (id === null) {
      if (!this.form.code.trim()) {
        this.toast.warning('Coupon code is required');
        this.isSubmitting.set(false);
        return;
      }
      const req: CreateCouponRequest = {
        code: this.form.code.trim().toUpperCase(),
        discountType: this.form.discountType,
        discountValue: this.form.discountValue!,
        minOrderValue: this.form.minOrderValue,
        maxDiscountValue: this.form.maxDiscountValue,
        usageLimit: this.form.usageLimit,
        startDate: this.form.startDate || null,
        endDate: this.form.endDate || null,
        status: this.form.status,
      };
      this.svc.createCoupon(req)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: res => {
            this.coupons.update(list => [res.data, ...list]);
            this.showForm.set(false);
            this.isSubmitting.set(false);
            this.toast.success('Coupon created');
          },
          error: err => {
            this.isSubmitting.set(false);
            this.toast.error(err?.error?.resolvedMessage ?? 'Failed to create coupon');
          },
        });
    } else {
      const req: UpdateCouponRequest = {
        discountType: this.form.discountType,
        discountValue: this.form.discountValue!,
        minOrderValue: this.form.minOrderValue,
        maxDiscountValue: this.form.maxDiscountValue,
        usageLimit: this.form.usageLimit,
        startDate: this.form.startDate || null,
        endDate: this.form.endDate || null,
        status: this.form.status,
      };
      this.svc.updateCoupon(id, req)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: res => {
            this.coupons.update(list => list.map(c => c.id === id ? res.data : c));
            this.showForm.set(false);
            this.isSubmitting.set(false);
            this.toast.success('Coupon updated');
          },
          error: err => {
            this.isSubmitting.set(false);
            this.toast.error(err?.error?.resolvedMessage ?? 'Failed to update coupon');
          },
        });
    }
  }

  // ── Delete ────────────────────────────────────────────────────────────────

  deleteCoupon(id: number): void {
    if (!confirm('Delete this coupon?')) return;
    this.deletingId.set(id);
    this.svc.deleteCoupon(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.coupons.update(list => list.filter(c => c.id !== id));
          this.deletingId.set(null);
          this.toast.success('Coupon deleted');
        },
        error: err => {
          this.deletingId.set(null);
          this.toast.error(err?.error?.resolvedMessage ?? 'Failed to delete coupon');
        },
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  statusClass(status: CouponStatus): string {
    return {
      ACTIVE: 'badge--success',
      EXPIRED: 'badge--default',
      DISABLED: 'badge--danger',
    }[status] ?? 'badge--default';
  }

  readonly skeletonRows = [1, 2, 3, 4, 5];
  readonly pages = (n: number) => Array.from({ length: n }, (_, i) => i);
}
