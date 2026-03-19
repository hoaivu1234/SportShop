import { Component, EventEmitter, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../../features/auth/services/auth.service';

@Component({
  selector: 'app-login-popup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-popup.component.html',
  styleUrl: './login-popup.component.css',
})
export class LoginPopupComponent {
  @Output() loginSuccess = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();

  form: FormGroup;
  showPassword = signal(false);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  togglePassword(): void {
    this.showPassword.update((v) => !v);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.form.value;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.loginSuccess.emit();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Login failed. Please check your credentials.',
        );
      },
    });
  }

  goToCreateAccount(): void {
    this.close.emit();
    this.router.navigate(['/auth/login']);
  }
}
