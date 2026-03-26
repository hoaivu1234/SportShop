import {
  Injectable,
  inject,
  signal,
  computed,
  effect,
  untracked,
  DestroyRef,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  Subject,
  debounceTime,
  distinctUntilChanged,
  switchMap,
  EMPTY,
  catchError,
  concatMap,
  tap,
  finalize,
} from 'rxjs';

import { AuthService } from '../../auth/services/auth.service';
import { CartService, MergeCartItem } from './cart.service';
import { GuestCartItem, GuestCartService } from './guest-cart.service';
import { CartResponse, CartItemResponse } from '../../../models/cart.model';

interface QtyUpdate {
  itemId:   number;
  quantity: number;
  /** snapshot of the cart before the optimistic update, for rollback */
  previous: CartResponse;
}

/**
 * CartStateService — single source of truth for cart state.
 *
 * Responsibilities:
 *  - Exposes a `cart` signal consumed by all cart-related components.
 *  - Routes operations to GuestCartService (not logged in) or
 *    CartService (logged in).
 *  - On login: merges localStorage cart → server cart, then clears localStorage.
 *  - Debounces quantity updates (400 ms per item) via per-item Subject/switchMap
 *    to prevent API flood on rapid clicks.
 *  - Optimistic updates: apply locally → API call → revert on error.
 */
@Injectable({ providedIn: 'root' })
export class CartStateService {
  private readonly authSvc      = inject(AuthService);
  private readonly cartSvc      = inject(CartService);
  private readonly guestSvc     = inject(GuestCartService);
  private readonly destroyRef   = inject(DestroyRef);

  // ── Public state ──────────────────────────────────────────────────────────

  readonly cart      = signal<CartResponse | null>(null);
  readonly isLoading = signal(false);
  readonly error     = signal<string | null>(null);

  readonly itemCount = computed(() =>
    this.cart()?.items.reduce((s, i) => s + i.quantity, 0) ?? 0
  );
  readonly isEmpty = computed(() => (this.cart()?.items.length ?? 0) === 0);

  // ── Per-item quantity debounce ────────────────────────────────────────────
  // Each item gets its own Subject so rapid clicks on different items don't
  // cancel each other's in-flight requests.

  private readonly qtySubjects = new Map<number, Subject<QtyUpdate>>();

  // ── Initialization ────────────────────────────────────────────────────────

  constructor() {
    // React to auth state changes.
    // untracked() ensures only loggedIn() is a dependency — signals read
    // inside onLogin/onLogout (e.g. isLoading) must not re-trigger this effect.
    effect(() => {
      const loggedIn = this.authSvc.loggedIn();
      untracked(() => {
        if (loggedIn) {
          this.onLogin();
        } else {
          this.onLogout();
        }
      });
    }, { allowSignalWrites: true });
  }

  // ── Public API ────────────────────────────────────────────────────────────

  /** Force-reloads the cart from the server (auth required) or localStorage. */
  load(): void {
    if (!this.authSvc.isLoggedIn()) {
      this.cart.set(this.guestSvc.asCartResponse());
      return;
    }
    if (this.isLoading()) return; // guard against concurrent fetches

    this.isLoading.set(true);
    this.error.set(null);

    this.cartSvc.getCart()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isLoading.set(false)),
      )
      .subscribe({
        next:  res => this.cart.set(res.data),
        error: ()  => this.error.set('Failed to load cart'),
      });
  }

  /**
   * Adds a variant to the cart.
   *
   * Guest path: synchronous localStorage update → signal update.
   * Auth path:  API call → signal update.
   * Returns the error message string if the operation fails, null on success.
   */
  addItem(
    variantId: number,
    quantity:  number,
    meta: Omit<GuestCartItem, 'id' | 'variantId' | 'quantity'>,
  ): void {
    if (!this.authSvc.isLoggedIn()) {
      const updated = this.guestSvc.addItem({ ...meta, variantId, quantity });
      this.cart.set(updated);
      return;
    }

    this.cartSvc.addItem(variantId, quantity)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next:  res => this.cart.set(res.data),
        error: err => this.error.set(
          err?.error?.resolvedMessage ?? 'Failed to add item to cart'
        ),
      });
  }

  /**
   * Updates a cart item's quantity with optimistic update + debounce.
   *
   * - The signal is updated immediately (optimistic).
   * - The API call is debounced by 400 ms per item.
   * - On API error the previous cart state is restored.
   */
  updateQuantity(itemId: number, quantity: number): void {
    if (quantity < 1) {
      this.removeItem(itemId);
      return;
    }

    const previous = this.cart();
    if (!previous) return;

    // Optimistic update
    this.cart.set(this.applyQtyOptimistic(previous, itemId, quantity));

    if (!this.authSvc.isLoggedIn()) {
      // Guest: localStorage is already updated above; persist it
      this.guestSvc.updateItem(itemId, quantity);
      return;
    }

    // Get or create a Subject for this item
    if (!this.qtySubjects.has(itemId)) {
      const subject$ = new Subject<QtyUpdate>();
      subject$.pipe(
        debounceTime(400),
        distinctUntilChanged((a, b) => a.quantity === b.quantity),
        // switchMap cancels any in-flight request for THIS item when a new qty arrives
        switchMap(update =>
          this.cartSvc.updateItem(update.itemId, update.quantity).pipe(
            tap(res => this.cart.set(res.data)),
            catchError(() => {
              // Revert to the snapshot taken before the optimistic update
              this.cart.set(update.previous);
              this.error.set('Failed to update quantity');
              return EMPTY;
            }),
          )
        ),
        takeUntilDestroyed(this.destroyRef),
      ).subscribe();
      this.qtySubjects.set(itemId, subject$);
    }

    this.qtySubjects.get(itemId)!.next({ itemId, quantity, previous });
  }

  /**
   * Removes a cart item.
   * Guest: sync localStorage removal.
   * Auth: API call with optimistic removal.
   */
  removeItem(itemId: number): void {
    const previous = this.cart();
    if (!previous) return;

    // Optimistic removal
    this.cart.set({
      ...previous,
      items: previous.items.filter(i => i.id !== itemId),
      totalItems: previous.totalItems - (previous.items.find(i => i.id === itemId)?.quantity ?? 0),
      totalPrice: previous.items
        .filter(i => i.id !== itemId)
        .reduce((s, i) => s + i.subtotal, 0),
    });

    if (!this.authSvc.isLoggedIn()) {
      this.guestSvc.removeItem(itemId);
      return;
    }

    this.cartSvc.removeItem(itemId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next:  res => this.cart.set(res.data),
        error: ()  => {
          this.cart.set(previous);
          this.error.set('Failed to remove item');
        },
      });
  }

  clearError(): void {
    this.error.set(null);
  }

  // ── Auth lifecycle ────────────────────────────────────────────────────────

  private onLogin(): void {
    const guestItems = this.guestSvc.getItems();

    if (guestItems.length > 0) {
      // Merge guest cart into server cart, then clear localStorage
      const mergePayload: MergeCartItem[] = guestItems.map(i => ({
        variantId: i.variantId,
        quantity:  i.quantity,
      }));

      this.cartSvc.mergeGuestCart(mergePayload)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: res => {
            this.cart.set(res.data);
            this.guestSvc.clear();
          },
          error: () => {
            // Merge failed: still load the server cart; guest items stay in localStorage
            // to retry on next login.
            this.load();
          },
        });
    } else {
      this.load();
    }
  }

  private onLogout(): void {
    // Clean up per-item subjects so they don't fire after logout
    this.qtySubjects.forEach(s => s.complete());
    this.qtySubjects.clear();
    // Show the (now-empty) guest cart
    this.cart.set(this.guestSvc.asCartResponse());
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  /** Returns a new CartResponse with the given item's quantity replaced. */
  private applyQtyOptimistic(
    cart: CartResponse,
    itemId: number,
    quantity: number,
  ): CartResponse {
    const items = cart.items.map(i =>
      i.id === itemId
        ? { ...i, quantity, subtotal: i.priceSnapshot * quantity }
        : i
    );
    return {
      ...cart,
      items,
      totalItems: items.reduce((s, i) => s + i.quantity, 0),
      totalPrice: items.reduce((s, i) => s + i.subtotal, 0),
    };
  }
}
