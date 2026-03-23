import { Component, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [NgClass],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css',
})
export class ToastComponent {
  private readonly toastService = inject(ToastService);

  readonly toasts = this.toastService.toasts;

  remove(id: string): void {
    this.toastService.remove(id);
  }

  trackById(_: number, toast: Toast): string {
    return toast.id;
  }
}
