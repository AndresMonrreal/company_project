import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivityAction } from '../models/activity-record.model';
import { ActivityFilterService } from '../services/activity-filter.service';

@Component({
  selector: 'app-activity-filter-bar',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm space-y-4">
      <!-- Tab strip -->
      <div class="flex gap-1 flex-wrap">
        @for (tab of tabs; track tab.value) {
          <button
            type="button"
            (click)="filterService.actionFilter.set(tab.value)"
            [class]="filterService.actionFilter() === tab.value
              ? 'px-4 py-1.5 rounded-full text-sm font-medium bg-indigo-600 text-white'
              : 'px-4 py-1.5 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200'"
          >{{ tab.label }}</button>
        }
      </div>

      <!-- Filters row -->
      <div class="flex flex-wrap items-center gap-3">
        <div class="flex items-center gap-2">
          <label class="text-xs text-gray-500">From</label>
          <input type="time" class="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            [value]="filterService.timeFrom()" (change)="filterService.timeFrom.set($any($event.target).value)" />
        </div>
        <div class="flex items-center gap-2">
          <label class="text-xs text-gray-500">To</label>
          <input type="time" class="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            [value]="filterService.timeTo()" (change)="filterService.timeTo.set($any($event.target).value)" />
        </div>
        <input type="text" placeholder="Search container or lot…" class="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 flex-1 min-w-40"
          [value]="filterService.searchTerm()" (input)="filterService.searchTerm.set($any($event.target).value)" />
        <button type="button" (click)="filterService.reset()" class="px-4 py-1.5 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50">
          Reset
        </button>
      </div>
    </div>
  `,
})
export class ActivityFilterBarComponent {
  protected readonly filterService = inject(ActivityFilterService);

  protected readonly tabs: Array<{ label: string; value: ActivityAction | 'ALL' }> = [
    { label: 'All', value: 'ALL' },
    { label: 'Reception', value: 'RECEPTION' },
    { label: 'Cut', value: 'CUT' },
    { label: 'Molding Output', value: 'MOLDING_OUTPUT' },
    { label: 'Scrap', value: 'SCRAP' },
  ];
}
