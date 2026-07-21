import { Injectable, signal } from '@angular/core';
import { ActivityAction, ActivityRecord } from '../models/activity-record.model';

@Injectable()
export class ActivityFilterService {
  readonly actionFilter = signal<ActivityAction | 'ALL'>('ALL');
  readonly timeFrom = signal<string>('');
  readonly timeTo = signal<string>('');
  readonly searchTerm = signal<string>('');

  filter(records: ActivityRecord[]): ActivityRecord[] {
    return records
      .filter(r => this.actionFilter() === 'ALL' || r.action === this.actionFilter())
      .filter(r => !this.timeFrom() || r.time >= this.timeFrom())
      .filter(r => !this.timeTo() || r.time <= this.timeTo())
      .filter(r => {
        const term = this.searchTerm().toLowerCase();
        return !term || (r.containerCode ?? '').toLowerCase().includes(term) || (r.profileCode ?? '').toLowerCase().includes(term);
      });
  }

  reset(): void {
    this.actionFilter.set('ALL');
    this.timeFrom.set('');
    this.timeTo.set('');
    this.searchTerm.set('');
  }
}
