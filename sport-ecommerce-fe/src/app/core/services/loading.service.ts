import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly activeRequests = signal<number>(0);

  /** True when one or more HTTP requests are in flight */
  readonly isLoading = computed(() => this.activeRequests() > 0);

  /** Current active request count (useful for testing) */
  readonly requestCount = computed(() => this.activeRequests());

  show(): void {
    this.activeRequests.update(count => count + 1);
  }

  hide(): void {
    this.activeRequests.update(count => Math.max(0, count - 1));
  }

  /** Force-reset state (e.g., after navigation or error recovery) */
  reset(): void {
    this.activeRequests.set(0);
  }
}
