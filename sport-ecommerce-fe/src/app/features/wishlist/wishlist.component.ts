import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { WishlistStateService } from './services/wishlist-state.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RouterLink],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css',
})
export class WishlistComponent implements OnInit {
  readonly wishlistState = inject(WishlistStateService);
  private readonly router = inject(Router);

  readonly skeletonItems = [1, 2, 3, 4];
  readonly fallbackImage = 'assets/images/placeholder.png';

  ngOnInit(): void {
    this.wishlistState.load();
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.fallbackImage;
  }

  goToProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
  }

  onRemove(id: number, event: Event): void {
    event.stopPropagation();
    // Toggle by product id — but we have the item id, so call through state service directly
    const item = this.wishlistState.items().find(w => w.id === id);
    if (item) this.wishlistState.toggle(item.productId);
  }
}
