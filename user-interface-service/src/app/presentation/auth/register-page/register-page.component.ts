import { Component, DestroyRef, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

const USERNAME_MIN_LENGTH: number = 7;
const PASSWORD_MIN_LENGTH: number = 7;

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss',
})
export class RegisterPageComponent {
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
      username: new FormControl<string>('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.minLength(USERNAME_MIN_LENGTH),
          Validators.pattern(/^[^@]*$/),
        ],
      }),
      password: new FormControl<string>('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.minLength(PASSWORD_MIN_LENGTH),
          Validators.pattern(/^(?=.*[0-9])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).*$/),
        ],
      }),
    });
  }

  get isUsernameRequiredMet(): boolean {
    return this.form.controls.username.value.trim().length > 0;
  }

  get isUsernameMinLengthMet(): boolean {
    return this.form.controls.username.value.trim().length >= USERNAME_MIN_LENGTH;
  }

  get isUsernameNoAtSymbolMet(): boolean {
    return !this.form.controls.username.value.includes('@');
  }

  get isPasswordRequiredMet(): boolean {
    return this.form.controls.password.value.trim().length > 0;
  }

  get isPasswordMinLengthMet(): boolean {
    return this.form.controls.password.value.length >= PASSWORD_MIN_LENGTH;
  }

  get isPasswordHasNumberMet(): boolean {
    return /[0-9]/.test(this.form.controls.password.value);
  }

  get isPasswordHasUppercaseMet(): boolean {
    return /[A-Z]/.test(this.form.controls.password.value);
  }

  get isPasswordHasSpecialSymbolMet(): boolean {
    return /[^A-Za-z0-9]/.test(this.form.controls.password.value);
  }

  submit(): void {
    this.errorMessage.set(null);

    const username: string = this.form.controls.username.value;
    const password: string = this.form.controls.password.value;

    const isUsernameValid: boolean =
      this.isUsernameRequiredMet && this.isUsernameMinLengthMet && this.isUsernameNoAtSymbolMet;
    const isPasswordValid: boolean =
      this.isPasswordRequiredMet &&
      this.isPasswordMinLengthMet &&
      this.isPasswordHasNumberMet &&
      this.isPasswordHasUppercaseMet &&
      this.isPasswordHasSpecialSymbolMet;

    if (!isUsernameValid || !isPasswordValid) {
      this.errorMessage.set('Please meet all username and password requirements.');
      return;
    }

    this.submitting.set(true);
    this.authManager
      .register(username.trim(), password)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          void this.router.navigateByUrl('/login');
        },
        error: () => {
          this.submitting.set(false);
          this.errorMessage.set('Registration failed. Please try a different username.');
        },
      });
  }
}

