import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ReportsApiClient } from '../data-access/reports-api.client';
import { ShiftOption } from '../models/reports.models';
import {
  ACTION_BADGE_COLOR,
  ActivityAction,
  ActivityRecord,
  ActivityStatus,
  statusBadgeColor,
} from '../../my-activity/models/activity-record.model';

@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Reports</h1>
        <p class="text-gray-500 mt-1 text-sm">Shift production summary and activity detail.</p>
      </div>

      @if (errorMessage()) {
        <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {{ errorMessage() }}
        </div>
      }

      <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        @if (shiftsLoading()) {
          <div class="flex justify-center py-4">
            <div class="h-6 w-6 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
          </div>
        } @else {
          <div class="flex flex-wrap items-end gap-4">
            <div class="flex-1 min-w-[200px]">
              <label for="shiftSelect" class="block text-sm font-medium text-gray-700 mb-1">Shift</label>
              <select
                id="shiftSelect"
                (change)="onShiftChange($any($event.target).value)"
                class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
              >
                <option value="">Select a shift</option>
                @for (shift of shifts(); track shift.id) {
                  <option [value]="shift.id">{{ shift.name }} ({{ shift.startTime }}–{{ shift.endTime }})</option>
                }
              </select>
            </div>
            <button
              (click)="loadReport()"
              [disabled]="selectedShiftId() === null || reportLoading()"
              class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              @if (reportLoading()) {
                <span class="flex items-center gap-2">
                  <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                  Loading…
                </span>
              } @else {
                Load Report
              }
            </button>
            @if (records().length > 0) {
              <button
                (click)="exportCsv()"
                class="rounded-lg border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors"
              >
                Export CSV
              </button>
            }
          </div>
        }
      </div>

      @if (records().length > 0) {
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-blue-600">{{ totalReceptions() }}</p>
            <p class="text-xs text-gray-500 mt-1">Receptions</p>
          </div>
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-yellow-600">{{ totalCuts() }}</p>
            <p class="text-xs text-gray-500 mt-1">Cuts</p>
          </div>
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-orange-600">{{ totalScrap() }}</p>
            <p class="text-xs text-gray-500 mt-1">Scrap</p>
          </div>
          <div class="bg-white rounded-xl border border-gray-200 p-4 text-center">
            <p class="text-2xl font-bold text-purple-600">{{ totalMolding() }}</p>
            <p class="text-xs text-gray-500 mt-1">Molding Outputs</p>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Time</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Container</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Profile</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Action</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Quantities</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              @for (r of records(); track r.id) {
                <tr class="hover:bg-gray-50">
                  <td class="px-4 py-3 text-sm text-gray-600">{{ r.time }}</td>
                  <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ r.containerCode }}</td>
                  <td class="px-4 py-3 text-sm text-gray-900">{{ r.profileCode }}</td>
                  <td class="px-4 py-3">
                    <span class="rounded-full px-2 py-0.5 text-xs font-medium {{ actionBadge(r.action) }}">
                      {{ r.action }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-sm text-gray-600">{{ r.quantities }}</td>
                  <td class="px-4 py-3">
                    @if (r.status) {
                      <span class="rounded-full px-2 py-0.5 text-xs font-medium {{ statusBadge(r.status) }}">
                        {{ r.status }}
                      </span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
})
export class ReportsPageComponent implements OnInit {
  private readonly reportsApi = inject(ReportsApiClient);

  readonly shifts = signal<ShiftOption[]>([]);
  readonly shiftsLoading = signal(true);
  readonly selectedShiftId = signal<number | null>(null);
  readonly records = signal<ActivityRecord[]>([]);
  readonly reportLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly totalReceptions = computed(() => this.records().filter((r) => r.action === 'RECEPTION').length);
  readonly totalCuts = computed(() => this.records().filter((r) => r.action === 'CUT').length);
  readonly totalScrap = computed(() => this.records().filter((r) => r.action === 'SCRAP').length);
  readonly totalMolding = computed(() => this.records().filter((r) => r.action === 'MOLDING_OUTPUT').length);

  ngOnInit(): void {
    this.reportsApi.getShifts().subscribe({
      next: (result) => {
        this.shifts.set(result);
        this.shiftsLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load shifts. Please refresh.');
        this.shiftsLoading.set(false);
      },
    });
  }

  onShiftChange(value: string): void {
    this.selectedShiftId.set(value ? +value : null);
  }

  loadReport(): void {
    if (this.selectedShiftId() === null || this.reportLoading()) return;
    this.reportLoading.set(true);
    this.errorMessage.set(null);
    this.reportsApi.getMyActivity(this.selectedShiftId()!).subscribe({
      next: (result) => {
        this.records.set(result);
        this.reportLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load report. Please try again.');
        this.reportLoading.set(false);
      },
    });
  }

  exportCsv(): void {
    const csv = this.records()
      .map((r) => [r.time, r.containerCode, r.profileCode, r.action, r.quantities, r.status ?? ''].join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'report.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  actionBadge(action: ActivityAction): string {
    return ACTION_BADGE_COLOR[action] ?? '';
  }

  statusBadge(status: ActivityStatus): string {
    return statusBadgeColor(status);
  }
}