import { Component, inject, signal } from '@angular/core';
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
            <div class="relative">
              <input
                formControlName="password"
                [type]="showPassword() ? 'text' : 'password'"
                autocomplete="current-password"
                class="w-full bg-gray-700 border border-gray-600 text-white rounded-lg px-4 py-2.5 pr-10 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="Enter password"
              />
              <button
                type="button"
                (click)="showPassword.set(!showPassword())"
                class="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-200"
              >
                @if (showPassword()) {
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.957 9.542 7a10.025 10.025 0 01-4.132 4.411m0 0L21 21" />
                  </svg>
                } @else {
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.957 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                }
              </button>
            </div>
          </div>
          @if (errorMessage) {
            <div class="text-red-400 text-sm bg-red-900/30 rounded-lg px-4 py-2.5">{{ errorMessage }}</div>
          }
          <button
            type="submit"
            [disabled]="loading"
            class="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg py-2.5 transition-colors"
          >
            {{ loading ? 'Signing in...' : 'Sign in' }}
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
  protected readonly showPassword = signal(false);

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