import { Component, EventEmitter, Output } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-dropdown',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-dropdown.component.html',
  styleUrl: './user-dropdown.component.css',
})
export class UserDropdownComponent {
  @Output() logout = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();
}
