import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RegisterMoldingOutputApiClient } from '../data-access/register-molding-output-api.client';
import { CuttingAvailableResult, MoldingOutputSuccessResponse } from '../models/register-molding-output.models';

@Component({
  selector: 'app-register-molding-output-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Register Molding Output</h1>
        <p class="text-gray-500 mt-1 text-sm">Scan a container to load the cut record, then record the molding output.</p>
      </div>

      @if (successResponse()) {
        <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">
          Molding output registered —
          Cutting Record: <strong>#{{ successResponse()!.cuttingRecordId }}</strong>,
          Quantity Sent: <strong>{{ successResponse()!.quantitySent }}</strong>
        </div>
      }

      @if (errorMessage()) {
        <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {{ errorMessage() }}
        </div>
      }

      <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 max-w-lg space-y-5">

        <!-- Container code scanner input -->
        <div>
          <label for="lot" class="block text-sm font-medium text-gray-700 mb-1">Lot Number</label>
          <div class="flex gap-2">
            <input id="lot" type="text"
                   [value]="lotValue()"
                   (input)="updateLotValue($event)"
                   (keydown.enter)="$event.preventDefault(); onLotLookup()"
                   placeholder="Scan or type lot number"
                   class="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <button type="button" (click)="onLotLookup()"
                    [disabled]="lookupLoading()"
                    class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
              @if (lookupLoading()) {
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent inline-block"></span>
              } @else {
                Search
              }
            </button>
          </div>
          @if (lookupError()) {
            <p class="mt-1 text-xs text-red-600">{{ lookupError() }}</p>
          }
        </div>

        <!-- Cutting record summary card -->
        @if (cuttingResult()) {
          <div class="rounded-lg border border-indigo-100 bg-indigo-50 p-4 text-sm space-y-1">
            <p class="font-semibold text-indigo-900">{{ cuttingResult()!.containerCode }}</p>
            <p class="text-indigo-700">Profile: {{ cuttingResult()!.profileCode }}</p>
            <p class="text-indigo-700">
              Quantities — Initial: {{ cuttingResult()!.initialQuantity }},
              Good: <strong>{{ cuttingResult()!.goodQuantity }}</strong>,
              Scrap: {{ cuttingResult()!.scrapQuantity }}
            </p>
            <p class="text-indigo-700">Cut at: {{ cuttingResult()!.cutAt }}</p>
          </div>

          <!-- Molding output form -->
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-5">

            <div>
              <label for="quantitySent" class="block text-sm font-medium text-gray-700 mb-1">
                Quantity Sent
                <span class="text-gray-400 font-normal">(max {{ cuttingResult()!.goodQuantity }})</span>
              </label>
              <input id="quantitySent" type="number" formControlName="quantitySent"
                     min="1" [attr.max]="cuttingResult()!.goodQuantity" step="1"
                     placeholder="Units sent to molding"
                     class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>

            <button type="submit"
                    [disabled]="submitting() || form.invalid"
                    class="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
              @if (submitting()) {
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                Registering...
              } @else {
                Register Molding Output
              }
            </button>

          </form>
        }

      </div>
    </div>
  `,
})
export class RegisterMoldingOutputPageComponent {
  private readonly api = inject(RegisterMoldingOutputApiClient);
  private readonly fb = inject(FormBuilder);

  readonly lotValue = signal('');
  readonly lookupLoading = signal(false);
  readonly cuttingResult = signal<CuttingAvailableResult | null>(null);
  readonly lookupError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly successResponse = signal<MoldingOutputSuccessResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    quantitySent: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  protected updateLotValue(event: Event): void {
    this.lotValue.set((event.target as HTMLInputElement).value);
  }

  protected onLotLookup(): void {
    const code = this.lotValue().trim();
    if (!code) return;

    this.lookupLoading.set(true);
    this.cuttingResult.set(null);
    this.lookupError.set(null);
    this.form.reset();

    this.api.getCuttingAvailable(code).subscribe({
      next: (result) => {
        this.cuttingResult.set(result);
        this.lookupLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.lookupLoading.set(false);
        if (err.status === 404) {
          this.lookupError.set(`No cut record found for lot: ${code}`);
        } else {
          this.lookupError.set('Failed to load cut record. Please try again.');
        }
      },
    });
  }

  onSubmit(): void {
    const cutting = this.cuttingResult();
    if (this.form.invalid || this.submitting() || !cutting) return;

    const { quantitySent } = this.form.getRawValue();
    if (quantitySent! > cutting.goodQuantity) {
      this.errorMessage.set(`Quantity sent cannot exceed available good quantity (${cutting.goodQuantity}).`);
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successResponse.set(null);

    this.api.register({
      cuttingRecordId: cutting.cuttingRecordId,
      quantitySent: quantitySent!,
    }).subscribe({
      next: (response) => {
        this.successResponse.set(response);
        this.submitting.set(false);
        this.form.reset();
        this.cuttingResult.set(null);
        this.lotValue.set('');
        setTimeout(() => this.successResponse.set(null), 3000);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if (err.status === 404) {
          this.errorMessage.set('No cut record found for this container.');
        } else if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else if (err.status === 403) {
          this.errorMessage.set('Insufficient permissions.');
        } else {
          this.errorMessage.set('Unexpected error. Please try again.');
        }
      },
    });
  }
}