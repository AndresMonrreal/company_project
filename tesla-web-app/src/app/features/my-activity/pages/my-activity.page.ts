import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { switchMap } from 'rxjs/operators';
import { ActivityFilterService } from '../services/activity-filter.service';
import { ActivityService } from '../services/activity.service';
import { ShiftApiClient } from '../data-access/shift-api.client';
import { ActivitySummaryCardsComponent } from '../components/activity-summary-cards.component';
import { ActivityFilterBarComponent } from '../components/activity-filter-bar.component';
import { ActivityTableComponent } from '../components/activity-table.component';
import { ActivityRecord } from '../models/activity-record.model';

@Component({
  selector: 'app-my-activity-page',
  standalone: true,
  providers: [ActivityFilterService],
  imports: [
    ActivitySummaryCardsComponent,
    ActivityFilterBarComponent,
    ActivityTableComponent,
  ],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold text-gray-900">My Activity</h1>
        <span class="inline-flex items-center rounded-full bg-green-100 text-green-800 text-xs font-medium px-3 py-1">
          {{ currentShiftName() }} · Active
        </span>
      </div>

      @if (isLoading()) {
        <div class="flex items-center justify-center py-12">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
        </div>
      }

      @if (error()) {
        <div class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {{ error() }}
        </div>
      }

      @if (!isLoading()) {
        <app-activity-summary-cards [records]="source()" />
        <app-activity-filter-bar />
        <app-activity-table [records]="filtered()" />
      }
    </div>
  `,
})
export class MyActivityPageComponent implements OnInit {
  private readonly filterService = inject(ActivityFilterService);
  private readonly activityService = inject(ActivityService);
  private readonly shiftApiClient = inject(ShiftApiClient);

  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly apiData = signal<ActivityRecord[]>([]);
  protected readonly currentShiftName = signal<string>('');

  protected readonly source = computed(() => this.apiData());
  protected readonly filtered = computed(() => this.filterService.filter(this.source()));

  ngOnInit(): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.shiftApiClient.getCurrentShift().pipe(
      switchMap(shift => {
        this.currentShiftName.set(shift.name);
        return this.activityService.loadMyActivity(shift.id);
      })
    ).subscribe({
      next: records => this.apiData.set(records),
      error: () => {
        this.error.set('Failed to load activity');
        this.isLoading.set(false);
      },
      complete: () => this.isLoading.set(false),
    });
  }
}