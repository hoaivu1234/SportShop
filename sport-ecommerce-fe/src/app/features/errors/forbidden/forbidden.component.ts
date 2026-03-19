import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './forbidden.component.html',
  styleUrl: './forbidden.component.css',
})
export class ForbiddenComponent {
  constructor(private router: Router, private location: Location) {}

  goBack(): void {
    this.router.navigate(['/']);
  }
}
