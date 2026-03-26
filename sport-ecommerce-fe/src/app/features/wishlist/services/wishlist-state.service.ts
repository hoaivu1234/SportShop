import {
  Injectable,
  DestroyRef,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { WishlistService } from './wishlist.service';
import { AuthService } from '../../auth/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { WishlistItemResponse } from '../../../models/wishlist.model';

@Injectable({ providedIn: 'root' })
export class WishlistStateService {
  private readonly wishlistSvc = inject(WishlistService);
  private readonly authSvc     = inject(AuthService);
  private readonly toast       = inject(ToastService);
  private readonly destroyRef  = inject(DestroyRef);

  // ── State ──────────────────────────────────────────────────────────────────
  readonly items      = signal<WishlistItemResponse[]>([]);
  readonly isLoading  = signal(false);
  readonly error      = signal<string | null>(null);
  readonly itemCount  = computed(() => this.items().length);

  constructor() {
    // Sync with auth state: load on login, clear on logout
    effect(() => {
      const loggedIn = this.authSvc.loggedIn();
      untracked(() => {
        if (loggedIn) this.load();
        else this.items.set([]);
      });
    }, { allowSignalWrites: true });
  }

  // ── Queries ────────────────────────────────────────────────────────────────

  /** Returns true when the product is in the current wishlist. Reactive — reads the items signal. */
  isInWishlist(productId: number): boolean {
    return this.items().some(w => w.productId === productId);
  }

  /** Returns the wishlist item id for removal, or null if not found. */
  getItemId(productId: number): number | null {
    return this.items().find(w => w.productId === productId)?.id ?? null;
  }

  // ── Commands ───────────────────────────────────────────────────────────────

  load(): void {
    if (!this.authSvc.loggedIn()) return;
    this.isLoading.set(true);
    this.wishlistSvc.getWishlist()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next:  res => { this.items.set(res.data ?? []); this.isLoading.set(false); },
        error: ()  => this.isLoading.set(false),
      });
  }

  toggle(productId: number, variantId?: number | null): void {
    if (!this.authSvc.loggedIn()) {
      this.toast.warning('Please log in to use your wishlist');
      return;
    }
    if (this.isInWishlist(productId)) {
      const id = this.getItemId(productId)!;
      this.remove(id, productId);
    } else {
      this.add(productId, variantId);
    }
  }

  // ── Private ────────────────────────────────────────────────────────────────

  private add(productId: number, variantId?: number | null): void {
    // Optimistic: nothing to add yet — wait for API response to push real item
    this.wishlistSvc.addItem({ productId, variantId })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.items.update(list => [res.data, ...list]);
          this.toast.success('Added to wishlist');
        },
        error: err => {
          const msg = err?.error?.resolvedMessage ?? 'Failed to add to wishlist';
          // Conflict (already in wishlist) — refresh to sync state
          if (err?.status === 409) this.load();
          else this.toast.error(msg);
        },
      });
  }

  private remove(id: number, productId: number): void {
    // Optimistic update: remove immediately, restore on error
    const snapshot = this.items();
    this.items.update(list => list.filter(w => w.id !== id));

    this.wishlistSvc.removeItem(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next:  () => this.toast.success('Removed from wishlist'),
        error: err => {
          this.items.set(snapshot); // rollback
          this.toast.error(err?.error?.resolvedMessage ?? 'Failed to remove from wishlist');
        },
      });
  }
}
