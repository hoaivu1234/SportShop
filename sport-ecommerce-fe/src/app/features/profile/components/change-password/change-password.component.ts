import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControlOptions } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { ToastService } from '../../../../core/services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
})
export class ChangePasswordComponent {
  private readonly userService = inject(UserService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  form: FormGroup = this.fb.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: [this.passwordMatchValidator] } as AbstractControlOptions
  );

  saving = false;
  showCurrent = false;
  showNew = false;
  showConfirm = false;

  passwordMatchValidator(form: FormGroup) {
    const np = form.get('newPassword')?.value;
    const cp = form.get('confirmPassword')?.value;
    return np === cp ? null : { passwordMismatch: true };
  }

  onSave(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const { currentPassword, newPassword } = this.form.value;
    this.userService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.toastService.success('Password updated successfully!');
        this.form.reset();
        this.saving = false;
      },
      error: (err: HttpErrorResponse) => {
        const msg = err.error?.resolvedMessage ?? 'Failed to update password.';
        this.toastService.error(msg);
        this.saving = false;
      },
    });
  }
}
