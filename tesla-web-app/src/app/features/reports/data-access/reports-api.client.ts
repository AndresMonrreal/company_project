import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import { ActivityRecord } from '../../my-activity/models/activity-record.model';
import { ShiftOption } from '../models/reports.models';

@Injectable({ providedIn: 'root' })
export class ReportsApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getShifts(): Observable<ShiftOption[]> {
    return this.http.get<ShiftOption[]>(`${this.apiBaseUrl}/api/shifts`);
  }

  getMyActivity(shiftId: number): Observable<ActivityRecord[]> {
    return this.http.get<ActivityRecord[]>(`${this.apiBaseUrl}/api/activity/my`, {
      params: { shiftId: shiftId.toString() },
    });
  }
}