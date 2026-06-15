import { Component, effect, inject, input, output, signal } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { ActivityRecord } from '../models/activity-record.model';
import { ActivityDetail, ActivityDetailApiClient } from '../data-access/activity-detail-api.client';

@Component({
  selector: 'app-activity-detail-modal',
  standalone: true,
  imports: [SlicePipe],
  template: `
    @if (record()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" (click)="onBackdropClick($event)">
        <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <h2 class="text-lg font-semibold text-gray-900">Activity Detail</h2>
            <button type="button" (click)="close.emit()" class="text-gray-400 hover:text-gray-700">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>
          <div class="px-6 py-5">
            @if (isLoading()) {
              <div class="flex justify-center py-8">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
              </div>
            } @else if (error()) {
              <p class="text-red-600 text-sm">{{ error() }}</p>
            } @else if (detail()) {
              <dl class="space-y-3 text-sm">
                @if (detail()!.type === 'RECEPTION') {
                  <div class="flex justify-between"><dt class="text-gray-500">Container</dt><dd class="font-mono text-gray-900">{{ asReception().containerCode }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Profile</dt><dd class="text-gray-900">{{ asReception().profileCode }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Lot</dt><dd class="text-gray-900">{{ asReception().lot }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Received Qty</dt><dd class="text-gray-900">{{ asReception().receivedQuantity }} pcs</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Status</dt><dd class="text-gray-900">{{ asReception().status }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Received At</dt><dd class="text-gray-900">{{ asReception().receivedAt | slice:0:16 }}</dd></div>
                }
                @if (detail()!.type === 'CUT') {
                  <div class="flex justify-between"><dt class="text-gray-500">Container</dt><dd class="font-mono text-gray-900">{{ asCutting().containerCode }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Profile</dt><dd class="text-gray-900">{{ asCutting().profileCode }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Machine</dt><dd class="text-gray-900">{{ asCutting().machineCode }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Initial Qty</dt><dd class="text-gray-900">{{ asCutting().initialQuantity }} pcs</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Good Qty</dt><dd class="text-gray-900">{{ asCutting().goodQuantity }} pcs</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Scrap Qty</dt><dd class="text-gray-900">{{ asCutting().scrapQuantity }} pcs</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Cut At</dt><dd class="text-gray-900">{{ asCutting().cutAt | slice:0:16 }}</dd></div>
                }
                @if (detail()!.type === 'SCRAP') {
                  <div class="flex justify-between"><dt class="text-gray-500">Cutting Record</dt><dd class="text-gray-900">#{{ asScrap().cuttingRecordId }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Quantity</dt><dd class="text-gray-900">{{ asScrap().quantity }} pcs</dd></div>
                  @if (asScrap().reason) {
                    <div class="flex justify-between"><dt class="text-gray-500">Reason</dt><dd class="text-gray-900">{{ asScrap().reason }}</dd></div>
                  }
                  <div class="flex justify-between"><dt class="text-gray-500">Created At</dt><dd class="text-gray-900">{{ asScrap().createdAt | slice:0:16 }}</dd></div>
                }
                @if (detail()!.type === 'MOLDING_OUTPUT') {
                  <div class="flex justify-between"><dt class="text-gray-500">Cutting Record</dt><dd class="text-gray-900">#{{ asMolding().cuttingRecordId }}</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Quantity Sent</dt><dd class="text-gray-900">{{ asMolding().quantitySent }} pcs</dd></div>
                  <div class="flex justify-between"><dt class="text-gray-500">Sent At</dt><dd class="text-gray-900">{{ asMolding().sentAt | slice:0:16 }}</dd></div>
                }
              </dl>
            }
          </div>
        </div>
      </div>
    }
  `,
})
export class ActivityDetailModalComponent {
  readonly record = input<ActivityRecord | null>(null);
  readonly close = output<void>();

  private readonly apiClient = inject(ActivityDetailApiClient);

  protected readonly isLoading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly detail = signal<ActivityDetail | null>(null);

  constructor() {
    effect(() => {
      const r = this.record();
      if (!r) {
        this.detail.set(null);
        return;
      }
      this.isLoading.set(true);
      this.error.set(null);
      this.detail.set(null);
      this.apiClient.getDetail(r.action, r.id).subscribe({
        next: d => { this.detail.set(d); this.isLoading.set(false); },
        error: () => { this.error.set('Failed to load detail.'); this.isLoading.set(false); },
      });
    });
  }

  protected onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.close.emit();
  }

  protected asReception() { return this.detail() as import('../data-access/activity-detail-api.client').ReceptionDetail; }
  protected asCutting() { return this.detail() as import('../data-access/activity-detail-api.client').CuttingDetail; }
  protected asScrap() { return this.detail() as import('../data-access/activity-detail-api.client').ScrapDetail; }
  protected asMolding() { return this.detail() as import('../data-access/activity-detail-api.client').MoldingOutputDetail; }
}
