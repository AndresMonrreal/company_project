import { Component, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  ACTION_BADGE_COLOR,
  ACTION_LABEL,
  ActivityAction,
  ActivityRecord,
  statusBadgeColor,
} from '../models/activity-record.model';
import { ActivityDetailModalComponent } from './activity-detail-modal.component';

@Component({
  selector: 'app-activity-table',
  standalone: true,
  imports: [RouterLink, ActivityDetailModalComponent],
  template: `
    @if (records().length === 0) {
      <div class="bg-white rounded-xl border border-gray-200 p-12 text-center shadow-sm">
        <p class="text-gray-500">No activity records match your filters.</p>
      </div>
    } @else {
      <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200">
              <th class="text-left px-4 py-3 font-medium text-gray-500">Time</th>
              <th class="text-left px-4 py-3 font-medium text-gray-500">Container</th>
              <th class="text-left px-4 py-3 font-medium text-gray-500">Lot</th>
              <th class="text-left px-4 py-3 font-medium text-gray-500">Profile</th>
              <th class="text-left px-4 py-3 font-medium text-gray-500">Action</th>
              <th class="text-left px-4 py-3 font-medium text-gray-500">Quantities</th>
              <th class="text-left px-4 py-3 font-medium text-gray-500">Status</th>
              <th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            @for (record of records(); track record.id) {
              <tr class="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 text-gray-700">{{ record.time }}</td>
                <td class="px-4 py-3">
                  <a [routerLink]="['/coming-soon']" class="text-indigo-600 hover:underline font-mono text-xs">{{ record.containerCode }}</a>
                </td>
                <td class="px-4 py-3 text-gray-700 font-mono text-xs">{{ record.lot ?? '—' }}</td>
                <td class="px-4 py-3 text-gray-700">{{ record.profileCode }}</td>
                <td class="px-4 py-3">
                  <span [class]="'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ' + actionBadgeColor(record.action)">
                    {{ actionLabel(record.action) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-gray-700">{{ record.quantities }}</td>
                <td class="px-4 py-3">
                  @if (record.status) {
                    <span [class]="'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ' + getStatusBadgeColor(record.status)">
                      {{ record.status }}
                    </span>
                  }
                </td>
                <td class="px-4 py-3 text-center">
                  <button type="button" (click)="selectedRecord.set(record); showDetail.set(true)" class="text-gray-400 hover:text-gray-700">&#x1F441;</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
    <app-activity-detail-modal [record]="selectedRecord()" [visible]="showDetail()" (closed)="showDetail.set(false)" />
  `,
})
export class ActivityTableComponent {
  readonly records = input<ActivityRecord[]>([]);

  protected readonly selectedRecord = signal<ActivityRecord | null>(null);
  protected readonly showDetail = signal(false);

  protected actionBadgeColor(action: ActivityAction): string {
    return ACTION_BADGE_COLOR[action];
  }

  protected actionLabel(action: ActivityAction): string {
    return ACTION_LABEL[action];
  }

  protected getStatusBadgeColor = statusBadgeColor;
}