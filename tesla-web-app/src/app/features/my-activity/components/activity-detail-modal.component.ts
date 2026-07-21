import { Component, effect, inject, input, output, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import { ActivityRecord } from '../models/activity-record.model';

@Component({
  selector: 'app-activity-detail-modal',
  standalone: true,
  imports: [],
  template: `
    @if (visible()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
           (click)="closed.emit()">
        <div class="bg-white rounded-xl shadow-xl max-w-md w-full mx-4 p-6"
             (click)="$event.stopPropagation()">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900">{{ actionTitle() }}</h2>
            <button type="button" (click)="closed.emit()"
                    class="text-gray-400 hover:text-gray-700 text-xl leading-none">&times;</button>
          </div>

          @if (loading()) {
            <div class="flex justify-center py-8">
              <div class="h-6 w-6 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
            </div>
          } @else if (detailError()) {
            <p class="text-sm text-red-600">{{ detailError() }}</p>
          } @else if (detail()) {
            <dl class="space-y-3 text-sm">
              @for (row of detailRows(); track row.label) {
                <div class="flex justify-between">
                  <dt class="text-gray-500">{{ row.label }}</dt>
                  <dd class="text-gray-900 font-medium">{{ row.value }}</dd>
                </div>
              }
            </dl>
          }
        </div>
      </div>
    }
  `,
})
export class ActivityDetailModalComponent {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  readonly record = input<ActivityRecord | null>(null);
  readonly visible = input<boolean>(false);
  readonly closed = output<void>();

  readonly loading = signal(false);
  readonly detailError = signal<string | null>(null);
  readonly detail = signal<any>(null);

  constructor() {
    effect(() => {
      const vis = this.visible();
      const rec = this.record();
      if (vis && rec) {
        this.fetchDetail(rec);
      } else {
        this.detail.set(null);
        this.detailError.set(null);
      }
    });
  }

  private fetchDetail(rec: ActivityRecord): void {
    const endpointMap: Record<string, string> = {
      RECEPTION: `${this.apiBaseUrl}/api/receptions/${rec.id}`,
      CUT: `${this.apiBaseUrl}/api/cutting/${rec.id}`,
      SCRAP: `${this.apiBaseUrl}/api/scrap/${rec.id}`,
      MOLDING_OUTPUT: `${this.apiBaseUrl}/api/molding-outputs/${rec.id}`,
    };
    const url = endpointMap[rec.action];
    if (!url) return;

    this.loading.set(true);
    this.detailError.set(null);
    this.detail.set(null);

    this.http.get<any>(url).subscribe({
      next: (data) => {
        this.detail.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.detailError.set('Failed to load details.');
        this.loading.set(false);
      },
    });
  }

  protected actionTitle(): string {
    const labels: Record<string, string> = {
      RECEPTION: 'Reception Detail',
      CUT: 'Cut Detail',
      SCRAP: 'Scrap Detail',
      MOLDING_OUTPUT: 'Molding Output Detail',
    };
    return this.record() ? (labels[this.record()!.action] ?? 'Detail') : 'Detail';
  }

  protected detailRows(): { label: string; value: string }[] {
    const d = this.detail();
    const rec = this.record();
    if (!d || !rec) return [];

    switch (rec.action) {
      case 'RECEPTION':
        return [
          { label: 'Container', value: d.containerCode ?? '—' },
          { label: 'Profile', value: d.profileCode ?? '—' },
          { label: 'Lot', value: d.lot ?? '—' },
          { label: 'Received Qty', value: d.receivedQuantity ?? '—' },
          { label: 'Status', value: d.status ?? '—' },
          { label: 'Received At', value: d.receivedAt ?? '—' },
        ];
      case 'CUT':
        return [
          { label: 'Container', value: d.containerCode ?? '—' },
          { label: 'Profile', value: d.profileCode ?? '—' },
          { label: 'Machine', value: d.machineName ?? '—' },
          { label: 'Initial Qty', value: d.initialQuantity ?? '—' },
          { label: 'Good Qty', value: d.goodQuantity ?? '—' },
          { label: 'Scrap Qty', value: d.scrapQuantity ?? '—' },
          { label: 'Cut At', value: d.cutAt ?? '—' },
        ];
      case 'SCRAP':
        return [
          { label: 'Shift', value: d.shiftName ?? '—' },
          { label: 'Profile', value: d.profileCode ?? '—' },
          { label: 'Quantity', value: d.quantity ?? '—' },
          { label: 'Reason', value: d.reason ?? '—' },
          { label: 'Created At', value: d.createdAt ?? '—' },
        ];
      case 'MOLDING_OUTPUT':
        return [
          { label: 'Cutting Record ID', value: d.cuttingRecordId ?? '—' },
          { label: 'Quantity Sent', value: d.quantitySent ?? '—' },
          { label: 'Sent At', value: d.sentAt ?? '—' },
        ];
      default:
        return [];
    }
  }
}