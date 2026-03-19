import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { StorageService } from '../../../../core/services/storage/storage.service';
import { ToastService } from '../../../../core/services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-profile-info',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-info.component.html',
  styleUrl: './profile-info.component.css',
})
export class ProfileInfoComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly storageService = inject(StorageService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  form: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phone: [''],
  });

  loading = false;
  saving = false;

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    const cached = this.storageService.getUserInfo() as any;
    if (cached) {
      this.form.patchValue({
        firstName: cached.firstName ?? '',
        lastName: cached.lastName ?? '',
        phone: cached.phone ?? '',
      });
    }

    this.loading = true;
    this.userService.getProfile().subscribe({
      next: (res) => {
        const user = res.data;
        this.form.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          phone: user.phone ?? '',
        });
        this.storageService.setUserInfo(user);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onSave(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const { firstName, lastName, phone } = this.form.value;
    this.userService.updateProfile({ firstName, lastName, phone }).subscribe({
      next: (res) => {
        this.storageService.setUserInfo(res.data);
        this.toastService.success('Profile updated successfully!');
        this.saving = false;
      },
      error: (err: HttpErrorResponse) => {
        const msg = err.error?.resolvedMessage ?? 'Failed to update profile.';
        this.toastService.error(msg);
        this.saving = false;
      },
    });
  }
}
