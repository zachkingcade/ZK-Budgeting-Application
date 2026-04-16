import { Component, DestroyRef, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss',
})
export class LoginPageComponent {
  readonly submitting = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form: FormGroup<{
    username: FormControl<string>;
    password: FormControl<string>;
  }>;

  constructor(
    private readonly authManager: AuthManagerService,
    private readonly router: Router,
    private readonly destroyRef: DestroyRef,
  ) {
    this.form = new FormGroup({
      username: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
      password: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    });
  }

  submit(): void {
    this.errorMessage.set(null);

    const username: string = this.form.controls.username.value;
    const password: string = this.form.controls.password.value;
    if (username.trim().length === 0 || password.trim().length === 0) {
      this.errorMessage.set('Username and password are required.');
      return;
    }

    this.submitting.set(true);
    this.authManager
      .login(username.trim(), password)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          void this.router.navigateByUrl('/ledger');
        },
        error: () => {
          this.submitting.set(false);
          this.errorMessage.set('Login failed. Please check your credentials and try again.');
        },
      });
  }
}

