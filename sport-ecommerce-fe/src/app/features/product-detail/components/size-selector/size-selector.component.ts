import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-size-selector',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './size-selector.component.html',
  styleUrl: './size-selector.component.css',
})
export class SizeSelectorComponent {
  @Input() selectedSize = '';
  @Input() sizes: string[] = ['UK 6', 'UK 7', 'UK 8', 'UK 9', 'UK 10', 'UK 11'];
  @Output() sizeSelected = new EventEmitter<string>();

  selectSize(size: string) {
    this.selectedSize = size;
    this.sizeSelected.emit(size);
  }
}
