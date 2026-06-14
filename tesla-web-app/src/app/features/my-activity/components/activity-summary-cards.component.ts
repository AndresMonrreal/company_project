import { Component, computed, input } from '@angular/core';
import { ActivityRecord } from '../models/activity-record.model';

@Component({
  selector: 'app-activity-summary-cards',
  standalone: true,
  imports: [],
  template: `
    <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <p class="text-sm text-gray-500">My Receptions</p>
        <p class="text-3xl font-bold text-gray-900 mt-1">{{ receptionCount() }}</p>
        <p class="text-xs text-gray-400 mt-1">this shift</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <p class="text-sm text-gray-500">My Cuts</p>
        <p class="text-3xl font-bold text-gray-900 mt-1">{{ cutCount() }}</p>
        <p class="text-xs text-gray-400 mt-1">this shift</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <p class="text-sm text-gray-500">My Scrap</p>
        <p class="text-3xl font-bold text-gray-900 mt-1">{{ scrapCount() }}</p>
        <p class="text-xs text-gray-400 mt-1">this shift</p>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <p class="text-sm text-gray-500">My Molding Outputs</p>
        <p class="text-3xl font-bold text-gray-900 mt-1">{{ moldingCount() }}</p>
        <p class="text-xs text-gray-400 mt-1">this shift</p>
      </div>
    </div>
  `,
})
export class ActivitySummaryCardsComponent {
  readonly records = input<ActivityRecord[]>([]);

  protected readonly receptionCount = computed(() => this.records().filter(r => r.action === 'RECEPTION').length);
  protected readonly cutCount = computed(() => this.records().filter(r => r.action === 'CUT').length);
  protected readonly scrapCount = computed(() => this.records().filter(r => r.action === 'SCRAP').length);
  protected readonly moldingCount = computed(() => this.records().filter(r => r.action === 'MOLDING_OUTPUT').length);
}
