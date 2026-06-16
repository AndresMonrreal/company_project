import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ActivityApiClient } from '../data-access/activity-api.client';
import { ActivityRecord } from '../models/activity-record.model';

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private readonly client = inject(ActivityApiClient);

  loadMyActivity(shiftId: number): Observable<ActivityRecord[]> {
    return this.client.getMyActivity(shiftId);
  }
}