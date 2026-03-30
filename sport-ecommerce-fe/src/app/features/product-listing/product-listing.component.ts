import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, distinctUntilChanged, debounceTime, switchMap, tap } from 'rxjs';
import { map } from 'rxjs/operators';

import { FilterSidebarComponent } from './components/filter-sidebar/filter-sidebar.component';
import { SortBarComponent } from './components/sort-bar/sort-bar.component';
import { ProductGridComponent } from './components/product-grid/product-grid.component';
import { ListingProduct } from './components/product-card/product-card.component';
import { ProductService } from '../admin/products/services/product.service';
import { CategoryService, CategoryTreeNode } from '../admin/categories/services/category.service';
import {
  ProductFilterState,
  SortKey,
  DEFAULT_FILTER,
  parseQueryParams,
  toQueryParams,
  toApiParams,
} from './models/product-filter.model';

export const PAGE_SIZE = 12;

@Component({
  selector: 'app-product-listing',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    FilterSidebarComponent,
    SortBarComponent,
    ProductGridComponent,
  ],
  templateUrl: './product-listing.component.html',
  styleUrl: './product-listing.component.css',
})
export class ProductListingComponent implements OnInit {
  private readonly route       = inject(ActivatedRoute);
  private readonly router      = inject(Router);
  private readonly productSvc  = inject(ProductService);
  private readonly categorySvc = inject(CategoryService);
  private readonly destroyRef  = inject(DestroyRef);

  // ── UI ────────────────────────────────────────────────────────────────────
  readonly pageSize = PAGE_SIZE;
  viewMode: 'grid' | 'list' = 'grid';
  email = '';

  // ── Reactive state (signals) ──────────────────────────────────────────────
  products      = signal<ListingProduct[]>([]);
  totalElements = signal(0);
  isLoading     = signal(true);
  filterState   = signal<ProductFilterState>(DEFAULT_FILTER);
  categoryTree  = signal<CategoryTreeNode[]>([]);

  /**
   * Keyword changes are funneled through this Subject so debounceTime
   * can be applied without debouncing all other filter changes.
   */
  private readonly keywordSubject$ = new Subject<string>();

  ngOnInit(): void {
    // ── 1. Load category tree once (cached in signal, never refetched) ──────
    this.categorySvc.getTreeCategories()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(res => this.categoryTree.set(res.data));

    // ── 2. Keyword debounce → URL update ─────────────────────────────────
    this.keywordSubject$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(keyword => this.navigate({ keyword, page: 0 }));

    // ── 3. URL query params → filter state → API (single reactive pipeline)
    //
    //   queryParams$ ──► parseQueryParams ──► distinctUntilChanged
    //                ──► tap(sync signal)  ──► tap(showLoading)
    //                ──► switchMap(API)    ──► update products signal
    //
    //   switchMap cancels any in-flight request when params change,
    //   so rapid filter changes never cause out-of-order responses.
    this.route.queryParams.pipe(
      map(params => parseQueryParams(params)),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      tap(state => {
        this.filterState.set(state);
        this.isLoading.set(true);
      }),
      switchMap(state =>
        this.productSvc.getProducts(toApiParams(state, PAGE_SIZE))
      ),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: res => {
        this.products.set(res.data.content.map(p => ({
          id:            p.id,
          name:          p.name,
          price:         p.discountPrice ?? p.price,
          originalPrice: p.discountPrice ? p.price : undefined,
          badge:         p.discountPrice ? 'Sale' as const : undefined,
          mainImageUrl:  p.mainImageUrl,
          totalStock:    p.totalStock,
        })));
        this.totalElements.set(res.data.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  // ── Event handlers ────────────────────────────────────────────────────────

  /** Called on every keystroke — debouncing happens in keywordSubject$ pipe. */
  onKeywordInput(keyword: string): void {
    this.keywordSubject$.next(keyword);
  }

  /**
   * Called by FilterSidebar for category / brand / price changes.
   * When the sidebar sets categoryId, clear any navbar-driven categorySlug
   * so the two are never active at the same time.
   */
  onFilterChange(patch: Partial<ProductFilterState>): void {
    const normalized = 'categoryId' in patch
      ? { ...patch, categorySlug: null }
      : patch;
    this.navigate({ ...normalized, page: 0 });
  }

  onSortChange(sort: SortKey): void {
    this.navigate({ sort, page: 0 });
  }

  onPageChange(page: number): void {
    this.navigate({ page });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  onViewModeChange(mode: 'grid' | 'list'): void {
    this.viewMode = mode;
  }

  onClearAll(): void {
    this.router.navigate([], { relativeTo: this.route, queryParams: {} });
  }

  removeChip(key: keyof ProductFilterState): void {
    const reset: Partial<ProductFilterState> = { page: 0 };
    if (key === 'keyword')      reset.keyword      = '';
    if (key === 'categoryId')   reset.categoryId   = null;
    if (key === 'categorySlug') { reset.categorySlug = null; reset.categoryId = null; }
    if (key === 'brand')        reset.brand        = null;
    if (key === 'minPrice')     reset.minPrice     = null;
    if (key === 'maxPrice')     reset.maxPrice     = null;
    if (key === 'onSale')       reset.onSale       = false;
    this.navigate(reset);
  }

  subscribeNewsletter(): void {
    this.email = '';
  }

  // ── Derived view helpers ──────────────────────────────────────────────────

  /** Active filter chips derived from current filter state. */
  get activeChips(): { label: string; key: keyof ProductFilterState }[] {
    const s = this.filterState();
    const chips: { label: string; key: keyof ProductFilterState }[] = [];

    if (s.keyword)
      chips.push({ label: `"${s.keyword}"`, key: 'keyword' });
    if (s.categorySlug)
      chips.push({ label: this.resolveCategoryNameBySlug(s.categorySlug), key: 'categorySlug' });
    else if (s.categoryId != null)
      chips.push({ label: this.resolveCategoryName(s.categoryId), key: 'categoryId' });
    if (s.brand)
      chips.push({ label: s.brand, key: 'brand' });
    if (s.minPrice != null)
      chips.push({ label: `From $${s.minPrice}`, key: 'minPrice' });
    if (s.maxPrice != null)
      chips.push({ label: `Up to $${s.maxPrice}`, key: 'maxPrice' });
    if (s.onSale)
      chips.push({ label: 'On Sale', key: 'onSale' });

    return chips;
  }

  get hasActiveFilters(): boolean {
    return this.activeChips.length > 0;
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  /**
   * Merge a partial patch into the current filter state and navigate.
   * This is the ONLY place that calls router.navigate — all state changes
   * flow through here, which makes the URL the single source of truth.
   */
  private navigate(patch: Partial<ProductFilterState>): void {
    const next: ProductFilterState = { ...this.filterState(), ...patch };
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: toQueryParams(next),
    });
  }

  /** Walk the category tree to resolve a category name from its ID. */
  private resolveCategoryName(id: number): string {
    for (const root of this.categoryTree()) {
      for (const child of root.children ?? []) {
        if (child.id === id) return child.name;
        for (const leaf of child.children ?? []) {
          if (leaf.id === id) return leaf.name;
        }
      }
    }
    return `Category ${id}`;
  }

  /** Walk the category tree to resolve a display name from a slug. */
  private resolveCategoryNameBySlug(slug: string): string {
    for (const root of this.categoryTree()) {
      if (root.slug === slug) return root.name;
      for (const child of root.children ?? []) {
        if (child.slug === slug) return child.name;
        for (const leaf of child.children ?? []) {
          if (leaf.slug === slug) return leaf.name;
        }
      }
    }
    // Capitalize the slug as a readable fallback
    return slug.charAt(0).toUpperCase() + slug.slice(1);
  }
}
