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

    // #region agent log
    fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'login-page.component.ts:64',message:'Login submit',data:{usernameLength:username.trim().length},timestamp:Date.now()})}).catch(()=>{});
    // #endregion

    this.submitting.set(true);
    this.authManager
      .login(username.trim(), password)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          // #region agent log
          fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'login-page.component.ts:76',message:'Login success',data:{},timestamp:Date.now()})}).catch(()=>{});
          // #endregion
          this.submitting.set(false);
          void this.router.navigateByUrl('/ledger');
        },
        error: () => {
          // #region agent log
          fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'login-page.component.ts:83',message:'Login error',data:{},timestamp:Date.now()})}).catch(()=>{});
          // #endregion
          this.submitting.set(false);
          this.errorMessage.set('Login failed. Please check your credentials and try again.');
        },
      });
  }
}

