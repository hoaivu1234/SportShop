import { Component, Input, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { WishlistStateService } from '../../../wishlist/services/wishlist-state.service';

export interface ListingProduct {
  id: number;
  name: string;
  /** Active display price — already the discounted price when on sale. */
  price: number;
  /** Original (pre-discount) price — rendered with strikethrough when present. */
  originalPrice?: number;
  badge?: 'Sale' | 'New';
  mainImageUrl?: string;
  totalStock?: number;
}

@Component({
  selector: 'app-listing-product-card',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.css',
})
export class ListingProductCardComponent {
  private readonly router       = inject(Router);
  readonly wishlistState        = inject(WishlistStateService);

  @Input() product!: ListingProduct;

  readonly fallbackImage = 'assets/images/placeholder.png';

  get imageUrl(): string {
    return this.product.mainImageUrl || this.fallbackImage;
  }

  get isOutOfStock(): boolean {
    return this.product.totalStock === 0;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallbackImage;
  }

  onAddToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.router.navigate(['/products', this.product.id]);
  }

  onToggleWishlist(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.wishlistState.toggle(this.product.id);
  }
}
