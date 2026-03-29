import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rating',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="rating">
      @for (star of stars; track $index) {
        <i
          class="fas"
          [class.fa-star]="star === 'full'"
          [class.fa-star-half-alt]="star === 'half'"
          [class.star--empty]="star === 'empty'"
        >
        </i>
      }
      @if (showCount && count !== undefined) {
        <span class="rating__count">({{ count }})</span>
      }
    </div>
  `,
  styles: [
    `
      .rating {
        display: inline-flex;
        align-items: center;
        gap: 2px;
        color: #f5a623;
        font-size: 13px;
      }
      .rating i.empty {
        color: #ddd;
      }
      .rating__count {
        font-size: 12px;
        color: var(--color-gray);
        margin-left: 4px;
      }
    `,
  ],
})
export class RatingComponent {
  @Input() value: number = 0;
  @Input() showCount = false;
  @Input() count?: number;

  get stars(): ('full' | 'half' | 'empty')[] {
    const result: ('full' | 'half' | 'empty')[] = [];
    for (let i = 1; i <= 5; i++) {
      if (this.value >= i) result.push('full');
      else if (this.value >= i - 0.5) result.push('half');
      else result.push('empty');
    }
    return result;
  }
}
