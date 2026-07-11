import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RegisterScrapApiClient } from '../data-access/register-scrap-api.client';
import { ProfileOption, ScrapSuccessResponse, ShiftOption } from '../models/register-scrap.models';

@Component({
  selector: 'app-register-scrap-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Register Scrap</h1>
        <p class="text-gray-500 mt-1 text-sm">Record scrap for the current shift and profile.</p>
      </div>

      @if (successResponse()) {
        <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">
          Scrap registered — Shift: <strong>{{ successResponse()!.shiftName }}</strong>,
          Profile: <strong>{{ successResponse()!.profileCode }}</strong>,
          Quantity: <strong>{{ successResponse()!.quantity }}</strong>
        </div>
      }

      @if (errorMessage()) {
        <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {{ errorMessage() }}
        </div>
      }

      <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 max-w-lg">
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-5">

          <div>
            <label for="shiftId" class="block text-sm font-medium text-gray-700 mb-1">Shift</label>
            <select id="shiftId" formControlName="shiftId"
                    class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="">Select shift…</option>
              @for (shift of shifts(); track shift.id) {
                <option [value]="shift.id">{{ shift.name }}</option>
              }
            </select>
          </div>

          <div>
            <label for="profileId" class="block text-sm font-medium text-gray-700 mb-1">Profile</label>
            <select id="profileId" formControlName="profileId"
                    class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="">Select profile…</option>
              @for (profile of profiles(); track profile.id) {
                <option [value]="profile.id">{{ profile.code }} — {{ profile.name }}</option>
              }
            </select>
          </div>

          <div>
            <label for="quantity" class="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
            <input id="quantity" type="number" formControlName="quantity"
                   min="1" step="1" placeholder="Units scrapped"
                   class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>

          <div>
            <label for="reason" class="block text-sm font-medium text-gray-700 mb-1">
              Reason <span class="text-gray-400 font-normal">(optional)</span>
            </label>
            <textarea id="reason" formControlName="reason" rows="3"
                      maxlength="255" placeholder="Describe reason for scrap…"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"></textarea>
          </div>

          <button type="submit"
                  [disabled]="submitting() || form.invalid"
                  class="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
            @if (submitting()) {
              <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
              Registering…
            } @else {
              Register Scrap
            }
          </button>

        </form>
      </div>
    </div>
  `,
})
export class RegisterScrapPage implements OnInit {
  private readonly api = inject(RegisterScrapApiClient);
  private readonly fb = inject(FormBuilder);

  readonly shifts = signal<ShiftOption[]>([]);
  readonly profiles = signal<ProfileOption[]>([]);
  readonly submitting = signal(false);
  readonly successResponse = signal<ScrapSuccessResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    shiftId: [null as number | null, Validators.required],
    profileId: [null as number | null, Validators.required],
    quantity: [null as number | null, [Validators.required, Validators.min(1)]],
    reason: [null as string | null],
  });

  ngOnInit(): void {
    this.api.getShifts().subscribe({ next: (s) => this.shifts.set(s) });
    this.api.getProfiles().subscribe({ next: (p) => this.profiles.set(p) });
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;

    const { shiftId, profileId, quantity, reason } = this.form.getRawValue();

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successResponse.set(null);

    this.api.register({
      shiftId: shiftId!,
      profileId: profileId!,
      quantity: quantity!,
      reason: reason ?? null,
    }).subscribe({
      next: (response) => {
        this.successResponse.set(response);
        this.submitting.set(false);
        this.form.patchValue({ profileId: null, quantity: null, reason: null });
        setTimeout(() => this.successResponse.set(null), 4000);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if (err.status === 422) {
          this.errorMessage.set(err.error?.message ?? 'Shift or profile is inactive.');
        } else if (err.status === 403) {
          this.errorMessage.set('Insufficient permissions.');
        } else if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else {
          this.errorMessage.set('Unexpected error. Please try again.');
        }
      },
    });
  }
}