import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-oauth2-redirect',
  standalone: true,
  template: '',
})
export class OAuth2RedirectComponent implements OnInit {
  constructor(
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    // Backend set HttpOnly cookies on redirect — verify by calling /me.
    // The interceptor sends cookies automatically via withCredentials.
    this.authService.getMe().subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: () => this.router.navigateByUrl('/auth/login'),
    });
  }
}
