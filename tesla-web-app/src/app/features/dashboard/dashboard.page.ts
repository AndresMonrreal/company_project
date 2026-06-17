import { Component, inject, OnInit, signal } from '@angular/core';
import { switchMap } from 'rxjs/operators';
import { AuthSession } from '../../core/auth/auth-session';
import { ShiftApiClient } from '../my-activity/data-access/shift-api.client';
import { ActivityApiClient } from '../my-activity/data-access/activity-api.client';
import { ActivityRecord } from '../my-activity/models/activity-record.model';
import { ActivitySummaryCardsComponent } from '../my-activity/components/activity-summary-cards.component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [ActivitySummaryCardsComponent],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
          @if (authSession.session()) {
            <p class="text-gray-600 mt-1">Welcome, {{ authSession.session()!.fullName }}</p>
          }
        </div>
        @if (shiftName()) {
          <span class="inline-flex items-center rounded-full bg-green-100 text-green-800 px-3 py-1 text-sm font-medium">
            {{ shiftName() }}
          </span>
        }
      </div>

      @if (isLoading()) {
        <div class="flex justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
        </div>
      } @else if (error()) {
        <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ error() }}</div>
      } @else {
        <app-activity-summary-cards [records]="records()" />
      }
    </div>
  `,
})
export class DashboardPageComponent implements OnInit {
  protected readonly authSession = inject(AuthSession);
  private readonly shiftApiClient = inject(ShiftApiClient);
  private readonly activityApiClient = inject(ActivityApiClient);

  readonly isLoading = signal(false);
  readonly error = signal<string | null>(null);
  readonly records = signal<ActivityRecord[]>([]);
  readonly shiftName = signal('');

  ngOnInit(): void {
    this.isLoading.set(true);
    this.shiftApiClient.getCurrentShift().pipe(
      switchMap(shift => {
        this.shiftName.set(shift.name);
        return this.activityApiClient.getMyActivity(shift.id);
      }),
    ).subscribe({
      next: (data) => {
        this.records.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('No active shift or failed to load data.');
        this.isLoading.set(false);
      },
    });
  }
}