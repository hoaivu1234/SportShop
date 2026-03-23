import { Component, input, output, computed, signal } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [NgClass],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css',
})
export class PaginationComponent {
  /** 0-based current page index (matches Spring Boot's page numbering) */
  currentPage = input.required<number>();
  pageSize    = input.required<number>();
  totalElements = input.required<number>();

  /** Emits the new 0-based page index */
  pageChange = output<number>();

  // ── Derived state ──────────────────────────────────────────────────────────

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.totalElements() / this.pageSize()))
  );

  readonly isFirst = computed(() => this.currentPage() === 0);
  readonly isLast  = computed(() => this.currentPage() >= this.totalPages() - 1);

  readonly startItem = computed(() =>
    this.totalElements() === 0 ? 0 : this.currentPage() * this.pageSize() + 1
  );
  readonly endItem = computed(() =>
    Math.min((this.currentPage() + 1) * this.pageSize(), this.totalElements())
  );

  /**
   * Returns an array of page numbers / ellipsis markers to render.
   * Always shows first, last, current ±1, with '...' gaps.
   */
  readonly pages = computed<(number | '...')[]>(() => {
    const total = this.totalPages();
    const cur   = this.currentPage();

    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i);
    }

    const visible = new Set<number>([0, total - 1, cur, cur - 1, cur + 1]
      .filter(p => p >= 0 && p < total));

    const sorted = [...visible].sort((a, b) => a - b);
    const result: (number | '...')[] = [];

    for (let i = 0; i < sorted.length; i++) {
      result.push(sorted[i]);
      if (i < sorted.length - 1 && sorted[i + 1] - sorted[i] > 1) {
        result.push('...');
      }
    }

    return result;
  });

  // ── Actions ────────────────────────────────────────────────────────────────

  goTo(page: number): void {
    if (page < 0 || page >= this.totalPages() || page === this.currentPage()) return;
    this.pageChange.emit(page);
  }

  prev(): void { this.goTo(this.currentPage() - 1); }
  next(): void { this.goTo(this.currentPage() + 1); }

  isEllipsis(p: number | '...'): p is '...' {
    return p === '...';
  }
}
