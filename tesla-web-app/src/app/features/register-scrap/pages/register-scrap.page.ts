import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RegisterScrapApiClient } from '../data-access/register-scrap-api.client';
import { ProfileOption, ScrapReportEntry, ScrapSuccessResponse, ShiftOption } from '../models/register-scrap.models';

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

      <!-- Scrap Report -->
      <div class="space-y-4">
        <div>
          <h2 class="text-xl font-bold text-gray-900">Scrap Report</h2>
          <p class="text-gray-500 text-sm mt-1">Unified scrap by shift (cutting + molding).</p>
        </div>

        @if (reportError()) {
          <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ reportError() }}</div>
        }

        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <div class="flex flex-wrap items-end gap-4">
            <div class="flex-1 min-w-[200px]">
              <label for="reportShiftSelect" class="block text-sm font-medium text-gray-700 mb-1">Shift</label>
              <select id="reportShiftSelect" (change)="onReportShiftChange($any($event.target).value)"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white">
                <option value="">Select a shift</option>
                @for (shift of shifts(); track shift.id) {
                  <option [value]="shift.id">{{ shift.name }}</option>
                }
              </select>
            </div>
            <button (click)="loadReport()" [disabled]="reportShiftId() === null || reportLoading()"
                    class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
              @if (reportLoading()) {
                <span class="flex items-center gap-2">
                  <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                  Loading…
                </span>
              } @else {
                Load Report
              }
            </button>
            @if (reportRows().length > 0) {
              <button (click)="exportCsv()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors">
                Export CSV
              </button>
            }
          </div>
        </div>

        @if (reportRows().length > 0) {
          <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Time</th>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Source</th>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Profile</th>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Machine</th>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Lot</th>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Qty</th>
                  <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Reason</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                @for (row of reportRows(); track row.id) {
                  <tr class="hover:bg-gray-50">
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.time }}</td>
                    <td class="px-4 py-3">
                      <span class="rounded-full px-2 py-0.5 text-xs font-medium {{ sourceBadge(row.source) }}">
                        {{ row.source === 'CUTTING' ? 'Corte' : 'Moldeo' }}
                      </span>
                    </td>
                    <td class="px-4 py-3 text-sm text-gray-900">{{ row.profileCode }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.machineName ?? '—' }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.lot ?? '—' }}</td>
                    <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ row.quantity }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ row.reason ?? '—' }}</td>
                  </tr>
                }
              </tbody>
              <tfoot class="bg-gray-50 border-t border-gray-200">
                <tr>
                  <td colspan="5" class="px-4 py-3 text-sm font-semibold text-gray-700">Total</td>
                  <td class="px-4 py-3 text-sm font-bold text-gray-900">{{ totalScrap() }}</td>
                  <td></td>
                </tr>
              </tfoot>
            </table>
          </div>
        }
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

  readonly reportShiftId = signal<number | null>(null);
  readonly reportRows = signal<ScrapReportEntry[]>([]);
  readonly reportLoading = signal(false);
  readonly reportError = signal<string | null>(null);

  readonly totalScrap = computed(() => this.reportRows().reduce((sum, r) => sum + r.quantity, 0));

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
        if (this.reportShiftId() !== null && this.reportShiftId() === response.shiftId) {
          this.loadReport();
        }
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

  onReportShiftChange(value: string): void {
    this.reportShiftId.set(value ? +value : null);
  }

  loadReport(): void {
    if (this.reportShiftId() === null || this.reportLoading()) return;
    this.reportLoading.set(true);
    this.reportError.set(null);
    this.api.getScrapReport(this.reportShiftId()!).subscribe({
      next: (rows) => {
        this.reportRows.set(rows);
        this.reportLoading.set(false);
      },
      error: () => {
        this.reportError.set('Failed to load scrap report. Please try again.');
        this.reportLoading.set(false);
      },
    });
  }

  exportCsv(): void {
    const escapeCsv = (val: string | null): string => {
      if (val === null || val === undefined) return '';
      return '"' + val.replace(/"/g, '""') + '"';
    };
    const csv = this.reportRows()
      .map((r) => [r.time, r.source, r.profileCode, r.machineName ?? '', r.lot ?? '', r.quantity.toString(), escapeCsv(r.reason)].join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'scrap-report.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  sourceBadge(source: 'CUTTING' | 'MOLDING'): string {
    return source === 'CUTTING'
      ? 'bg-orange-100 text-orange-700'
      : 'bg-purple-100 text-purple-700';
  }
}
