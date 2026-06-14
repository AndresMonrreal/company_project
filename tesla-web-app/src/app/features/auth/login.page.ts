import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="min-h-screen bg-gray-900 flex items-center justify-center px-4">
      <div class="w-full max-w-sm bg-gray-800 rounded-2xl shadow-2xl p-8">
        <div class="text-center mb-8">
          <h1 class="text-2xl font-bold text-white">RubberTrace</h1>
          <p class="text-xs text-gray-400 tracking-widest mt-1">OPERATIONS</p>
        </div>
        <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">Username</label>
            <input
              formControlName="username"
              type="text"
              autocomplete="username"
              class="w-full bg-gray-700 border border-gray-600 text-white rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              placeholder="Enter username"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">Password</label>
            <input
              formControlName="password"
              type="password"
              autocomplete="current-password"
              class="w-full bg-gray-700 border border-gray-600 text-white rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              placeholder="Enter password"
            />
          </div>
          @if (errorMessage) {
            <div class="text-red-400 text-sm bg-red-900/30 rounded-lg px-4 py-2.5">{{ errorMessage }}</div>
          }
          <button
            type="submit"
            [disabled]="loading"
            class="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg py-2.5 transition-colors"
          >
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </button>
        </form>
      </div>
    </div>
  `,
})
export class LoginPageComponent {
  private readonly authService = inject(AuthService);

  protected readonly form = new FormGroup({
    username: new FormControl('', { validators: [Validators.required, Validators.minLength(1)], nonNullable: true }),
    password: new FormControl('', { validators: [Validators.required, Validators.minLength(1)], nonNullable: true }),
  });
  protected loading = false;
  protected errorMessage: string | null = null;

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    const { username, password } = this.form.controls;

    this.authService.login(username.value, password.value).subscribe({
      error: (error: unknown) => {
        this.loading = false;
        if (error instanceof HttpErrorResponse && error.status === 401) {
          this.errorMessage = 'Invalid credentials. Please try again.';
        } else {
          this.errorMessage = 'Connection error. Check your network.';
        }
      },
      complete: () => {
        this.loading = false;
      },
    });
  }
}
