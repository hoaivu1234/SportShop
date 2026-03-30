import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryTreeNode } from '../../../admin/categories/services/category.service';
import {
  ProductFilterState,
  MAX_PRICE_CAP,
} from '../../models/product-filter.model';

/**
 * Flat category option used for rendering the sidebar filter list.
 * Groups Level-2 categories under their Level-1 parent name.
 */
interface CategoryGroup {
  parentName: string;
  items: { id: number; name: string }[];
}

@Component({
  selector: 'app-filter-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filter-sidebar.component.html',
  styleUrl: './filter-sidebar.component.css',
})
export class FilterSidebarComponent implements OnChanges {
  /**
   * Current filter state — drives which checkboxes / inputs are selected.
   * Passed down from the parent so the URL is the source of truth.
   */
  @Input() filterState!: ProductFilterState;

  /** Full category tree for building dynamic category groups. */
  @Input() categoryTree: CategoryTreeNode[] = [];

  /** Emits a partial state patch whenever the user changes any filter. */
  @Output() filterChange = new EventEmitter<Partial<ProductFilterState>>();

  /** Keyword input changes are debounced by the parent — emitted immediately. */
  @Output() keywordChange = new EventEmitter<string>();

  // ── Local view state (mirrors filterState to keep inputs two-way bound) ──

  localKeyword    = '';
  localCategoryId: number | null = null;
  /** Slider values — committed to URL only on mouseup (avoids API flood). */
  localMinPrice = 0;
  localMaxPrice = MAX_PRICE_CAP;

  readonly maxPriceCap = MAX_PRICE_CAP;

  // ── Derived: build category groups from tree ──────────────────────────────

  get categoryGroups(): CategoryGroup[] {
    return this.categoryTree.map(root => ({
      parentName: root.name,
      // Level-2 items are the direct children
      items: (root.children ?? []).map(child => ({
        id:   child.id,
        name: child.name,
      })),
    }));
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  /**
   * Sync local state from the incoming filterState.
   * This keeps the UI in sync when the user navigates back/forward
   * or shares a URL — the filter state flows DOWN from the URL.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['filterState']) {
      const s = this.filterState;
      this.localKeyword    = s.keyword;
      this.localCategoryId = s.categoryId;
      this.localMinPrice   = s.minPrice ?? 0;
      this.localMaxPrice   = s.maxPrice ?? this.maxPriceCap;
    }
  }

  onSaleToggle(): void {
    this.filterChange.emit({ onSale: !this.filterState.onSale });
  }

  // ── Event emitters ────────────────────────────────────────────────────────

  onKeywordInput(value: string): void {
    this.keywordChange.emit(value);
  }

  onCategoryToggle(id: number): void {
    const next = this.localCategoryId === id ? null : id;
    this.localCategoryId = next;
    this.filterChange.emit({ categoryId: next });
  }

  /**
   * Commit price range to URL only when the user releases the slider.
   * Using (change) instead of (input) prevents an API call on every pixel.
   */
  onPriceCommit(): void {
    this.filterChange.emit({
      minPrice: this.localMinPrice > 0               ? this.localMinPrice : null,
      maxPrice: this.localMaxPrice < this.maxPriceCap ? this.localMaxPrice : null,
    });
  }

  clearAll(): void {
    this.filterChange.emit({
      keyword:      '',
      categoryId:   null,
      categorySlug: null,
      minPrice:     null,
      maxPrice:     null,
      onSale:       false,
    });
    this.keywordChange.emit('');
  }
}
