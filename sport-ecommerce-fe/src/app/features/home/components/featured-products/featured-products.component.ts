import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProductCardComponent, ProductCard } from '../../../../shared/components/product-card/product-card.component';
import { ProductService } from '../../../admin/products/services/product.service';
import { CategoryService, CategoryTreeNode } from '../../../admin/categories/services/category.service';
import { ReviewService } from '../../../product-detail/services/review.service';

/** Extends ProductCard with a sport tag derived from the category hierarchy. */
interface TrendingProduct extends ProductCard {
  /** First word (lowercase) of the Level-2 category: "football" | "fitness" | "swimming" | "all" */
  sport: string;
}

@Component({
  selector: 'app-featured-products',
  standalone: true,
  imports: [CommonModule, RouterModule, ProductCardComponent],
  templateUrl: './featured-products.component.html',
  styleUrl: './featured-products.component.css',
})
export class FeaturedProductsComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly reviewService = inject(ReviewService);

  activeTab = signal<string>('all');

  /** Populated after the API call completes — starts with "All" only. */
  tabs: { key: string; label: string }[] = [{ key: 'all', label: 'All' }];

  private allProducts: TrendingProduct[] = [];
  isLoading = true;

  /** Re-evaluated by Angular's CD whenever activeTab() changes. */
  get filteredProducts(): ProductCard[] {
    const tab = this.activeTab();
    return tab === 'all'
      ? this.allProducts
      : this.allProducts.filter(p => p.sport === tab);
  }

  ngOnInit(): void {
    // Load tree and products in parallel — tree provides the sport→categoryId mapping
    forkJoin({
      tree: this.categoryService.getTreeCategories(),
      products: this.productService.getProducts({
        page: 0,
        size: 8,
        sort: 'createdAt',
        direction: 'desc',
        status: 'ACTIVE',
      }),
    }).subscribe({
      next: ({ tree, products }) => {
        const tagMap = this.buildTagMap(tree.data);

        // Derive tabs from unique sport names (Level-2 first word)
        const sportNames = new Set<string>();
        for (const root of tree.data) {
          for (const domain of root.children ?? []) {
            sportNames.add(domain.name.split(' ')[0].toLowerCase());
          }
        }
        this.tabs = [
          { key: 'all', label: 'All' },
          ...[...sportNames].map(s => ({
            key: s,
            label: s.charAt(0).toUpperCase() + s.slice(1),
          })),
        ];

        this.allProducts = products.data.content.map(p => {
          // price    = the full/regular price (shown as crossed-out if a sale price exists)
          // discountPrice = the LOWER sale price shown as the active price
          const displayPrice  = p.discountPrice ?? p.price;
          const originalPrice = p.discountPrice ? p.price : undefined;
          return {
            id: p.id,
            name: p.name,
            price: displayPrice,
            originalPrice,
            image: p.mainImageUrl ?? 'assets/images/placeholder.png',
            category: p.categoryName ?? '',
            rating: 0,
            reviewCount: 0,
            badge: p.discountPrice ? 'SALE' : 'NEW',
            sport: tagMap.get(p.categoryId ?? 0) ?? 'all',
          };
        });

        // Load review data for each product
        this.allProducts.forEach(product => {
          this.reviewService.getProductSummary(product.id).subscribe({
            next: ({ data }) => {
              product.rating = data.averageRating;
              product.reviewCount = data.totalReviews;
            }
          });
        });

        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  /**
   * Builds a Map<leafCategoryId, sportName> by traversing the tree.
   *
   * Hierarchy:
   *   Level-1 (root): Shoes, Clothing, Accessories
   *   Level-2 (domain): Football Shoes, Fitness Clothing …  ← sport = first word (lowercase)
   *   Level-3 (leaf): Firm Ground Boots, Gym T-shirt …      ← what products reference
   *
   * "Football Shoes" → sport = "football"
   * "Fitness Clothing" → sport = "fitness"
   * "Swimming Accessories" → sport = "swimming"
   */
  private buildTagMap(tree: CategoryTreeNode[]): Map<number, string> {
    const map = new Map<number, string>();
    for (const root of tree) {
      for (const domain of root.children ?? []) {
        const sport = domain.name.split(' ')[0].toLowerCase();
        for (const leaf of domain.children ?? []) {
          map.set(leaf.id, sport);
        }
      }
    }
    return map;
  }

  setTab(key: string): void {
    this.activeTab.set(key);
  }
}
