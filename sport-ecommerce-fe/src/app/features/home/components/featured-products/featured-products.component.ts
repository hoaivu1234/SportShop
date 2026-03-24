import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductCardComponent, ProductCard } from '../../../../shared/components/product-card/product-card.component';
import { ProductService } from '../../../admin/products/services/product.service';

type TabKey = 'all' | 'running' | 'training' | 'sports';

@Component({
  selector: 'app-featured-products',
  standalone: true,
  imports: [CommonModule, ProductCardComponent],
  templateUrl: './featured-products.component.html',
  styleUrl: './featured-products.component.css',
})
export class FeaturedProductsComponent implements OnInit {
  private readonly productService = inject(ProductService);

  activeTab = signal<TabKey>('all');

  tabs: { key: TabKey; label: string }[] = [
    { key: 'all', label: 'All' },
    { key: 'running', label: 'Running' },
    { key: 'training', label: 'Training' },
    { key: 'sports', label: 'Team Sports' },
  ];

  allProducts: (ProductCard & { tags: TabKey[] })[] = [];

  ngOnInit(): void {
    this.productService.getProducts({
      page: 0,
      size: 8,
      sort: 'createdAt',
      direction: 'desc',
      status: 'ACTIVE',
    }).subscribe({
      next: (res) => {
        this.allProducts = res.data.content.map(p => ({
          id: p.id,
          name: p.name,
          price: p.price,
          originalPrice: p.discountPrice,
          image: p.mainImageUrl || 'assets/images/placeholder.png',
          category: p.categoryName || 'Sport',
          rating: 0,
          reviewCount: 0,
          tags: this.resolveTags(p.categoryName),
        }));
      },
    });
  }

  private resolveTags(categoryName?: string): TabKey[] {
    const tags: TabKey[] = ['all'];
    if (!categoryName) return tags;
    const lower = categoryName.toLowerCase();
    if (lower.includes('run')) tags.push('running');
    if (lower.includes('train') || lower.includes('gym') || lower.includes('fitness')) tags.push('training');
    if (lower.includes('sport') || lower.includes('team') || lower.includes('basket') || lower.includes('football') || lower.includes('soccer')) tags.push('sports');
    return tags;
  }

  get filteredProducts(): ProductCard[] {
    const tab = this.activeTab();
    return this.allProducts.filter((p) => p.tags.includes(tab));
  }

  setTab(tab: TabKey): void {
    this.activeTab.set(tab);
  }
}
