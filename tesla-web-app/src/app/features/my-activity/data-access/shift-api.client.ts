import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';

export interface CurrentShiftResponse {
  id: number;
  name: string;
  startTime: string;
  endTime: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class ShiftApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getCurrentShift(): Observable<CurrentShiftResponse> {
    return this.http.get<CurrentShiftResponse>(`${this.apiBaseUrl}/api/shifts/current`);
  }
}