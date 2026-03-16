import { Injectable, signal, computed } from '@angular/core';
import { TOAST_CONFIG } from '../constants/app.constant';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  message: string;
  title?: string;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly _toasts = signal<Toast[]>([]);

  /** Read-only signal of active toasts for the template layer */
  readonly toasts = computed(() => this._toasts());

  show(message: string, type: ToastType = 'info', title?: string, duration?: number): void {
    const resolvedDuration =
      duration ?? (type === 'error' ? TOAST_CONFIG.ERROR_DURATION_MS : TOAST_CONFIG.DURATION_MS);

    const toast: Toast = {
      id: crypto.randomUUID(),
      type,
      message,
      title,
      duration: resolvedDuration,
    };

    this._toasts.update(list => [...list, toast].slice(-TOAST_CONFIG.MAX_TOASTS));

    setTimeout(() => this.remove(toast.id), resolvedDuration);
  }

  success(message: string, title?: string, duration?: number): void {
    this.show(message, 'success', title, duration);
  }

  error(message: string, title?: string, duration?: number): void {
    this.show(message, 'error', title ?? 'Lỗi', duration);
  }

  warning(message: string, title?: string, duration?: number): void {
    this.show(message, 'warning', title, duration);
  }

  info(message: string, title?: string, duration?: number): void {
    this.show(message, 'info', title, duration);
  }

  remove(id: string): void {
    this._toasts.update(list => list.filter(t => t.id !== id));
  }

  clear(): void {
    this._toasts.set([]);
  }
}
