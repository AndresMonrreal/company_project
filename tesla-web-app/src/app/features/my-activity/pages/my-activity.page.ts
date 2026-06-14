import { Component, computed, inject, signal } from '@angular/core';
import { ACTIVITY_EMPTY_DATA, ACTIVITY_MOCK_DATA } from '../data-access/activity-mock.data';
import { ActivityFilterService } from '../services/activity-filter.service';
import { ActivitySummaryCardsComponent } from '../components/activity-summary-cards.component';
import { ActivityFilterBarComponent } from '../components/activity-filter-bar.component';
import { ActivityTableComponent } from '../components/activity-table.component';

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
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">My Activity</h1>
        </div>
        <span class="inline-flex items-center rounded-full bg-green-100 text-green-800 text-xs font-medium px-3 py-1">
          Shift A · Active
        </span>
      </div>

      <!-- Dev toggle -->
      <div>
        <button type="button" (click)="populated.set(!populated())"
          class="text-xs px-3 py-1.5 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50">
          Toggle state: {{ populated() ? 'populated' : 'empty' }}
        </button>
      </div>

      <!-- Summary cards -->
      <app-activity-summary-cards [records]="source()" />

      <!-- Filter bar -->
      <app-activity-filter-bar />

      <!-- Table -->
      <app-activity-table [records]="filtered()" />
    </div>
  `,
})
export class MyActivityPageComponent {
  private readonly filterService = inject(ActivityFilterService);

  protected readonly populated = signal(true);
  protected readonly source = computed(() => this.populated() ? ACTIVITY_MOCK_DATA : ACTIVITY_EMPTY_DATA);
  protected readonly filtered = computed(() => this.filterService.filter(this.source()));
}
