import { Component, Input, inject, signal, effect, untracked } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SizeSelectorComponent } from '../size-selector/size-selector.component';
import { ProductDetailResponse, VariantResponse } from '../../../../models/product.model';
import { CartStateService } from '../../../cart/services/cart-state.service';
import { WishlistStateService } from '../../../wishlist/services/wishlist-state.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-product-info',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink, SizeSelectorComponent],
  templateUrl: './product-info.component.html',
  styleUrl: './product-info.component.css',
})
export class ProductInfoComponent {
  private readonly cartState    = inject(CartStateService);
  readonly wishlistState        = inject(WishlistStateService);
  private readonly toast        = inject(ToastService);

  @Input() product: ProductDetailResponse | null = null;

  selectedVariant: VariantResponse | null = null;
  selectedSize = '';
  quantity = 1;
  readonly isAdding = signal(false);

  constructor() {
    effect(() => {
      this.cartState.cart();
      const error = this.cartState.error();

      untracked(() => {
        if (!this.isAdding()) return;
        this.isAdding.set(false);
        if (error) {
          this.toast.error(error);
          this.cartState.clearError();
        } else {
          this.toast.success('Added to cart!');
        }
      });
    }, { allowSignalWrites: true });
  }

  // ── Derived state ──────────────────────────────────────────────────────────

  get uniqueSizes(): string[] {
    if (!this.product?.variants) return [];
    return [...new Set(this.product.variants.filter(v => v.size).map(v => v.size!))];
  }

  get uniqueColors(): string[] {
    if (!this.product?.variants) return [];
    return [...new Set(this.product.variants.filter(v => v.color).map(v => v.color!))];
  }

  get selectedColor(): string {
    return this.selectedVariant?.color ?? this.uniqueColors[0] ?? '';
  }

  get currentPrice(): number {
    return this.selectedVariant?.price ?? this.product?.price ?? 0;
  }

  get firstSku(): string {
    return this.product?.variants?.[0]?.sku ?? '';
  }

  get activeVariant(): VariantResponse | null {
    return this.selectedVariant ?? this.product?.variants?.[0] ?? null;
  }

  get isOutOfStock(): boolean {
    return (this.activeVariant?.stock ?? 0) === 0;
  }

  get isWishlisted(): boolean {
    return !!this.product && this.wishlistState.isInWishlist(this.product.id);
  }

  // ── Event handlers ────────────────────────────────────────────────────────

  onSizeSelected(size: string): void {
    this.selectedSize = size;
    this.selectedVariant = this.product?.variants.find(v => v.size === size) ?? null;
  }

  onColorSelected(color: string): void {
    this.selectedVariant = this.product?.variants.find(v => v.color === color) ?? null;
  }

  decreaseQty(): void {
    if (this.quantity > 1) this.quantity--;
  }

  increaseQty(): void {
    this.quantity++;
  }

  toggleWishlist(): void {
    if (!this.product) return;
    this.wishlistState.toggle(this.product.id, this.activeVariant?.id ?? null);
  }

  addToCart(): void {
    if (this.isAdding() || !this.product) return;

    const variant = this.activeVariant;
    if (!variant) {
      this.toast.warning('Please select a variant first');
      return;
    }
    if (variant.stock === 0) {
      this.toast.warning('This item is out of stock');
      return;
    }

    this.isAdding.set(true);

    const imageUrl =
      this.product.images.find(img => img.isMain)?.imageUrl ??
      this.product.images[0]?.imageUrl ??
      null;

    this.cartState.addItem(variant.id, this.quantity, {
      productId:   this.product.id,
      productName: this.product.name,
      sku:         variant.sku,
      size:        variant.size   ?? null,
      color:       variant.color  ?? null,
      imageUrl,
      price:       variant.price,
    });
  }
}
