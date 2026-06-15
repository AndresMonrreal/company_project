import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import { ActivityRecord } from '../models/activity-record.model';

@Injectable({ providedIn: 'root' })
export class ActivityApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getMyActivity(shiftId: number): Observable<ActivityRecord[]> {
    return this.http.get<ActivityRecord[]>(`${this.apiBaseUrl}/api/activity/my`, {
      params: { shiftId: shiftId.toString() },
    });
  }
}