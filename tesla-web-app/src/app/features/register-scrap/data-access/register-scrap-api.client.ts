import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import {
  ProfileOption,
  RegisterScrapRequest,
  ScrapReportEntry,
  ScrapSuccessResponse,
  ShiftOption,
} from '../models/register-scrap.models';

@Injectable({ providedIn: 'root' })
export class RegisterScrapApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getShifts(): Observable<ShiftOption[]> {
    return this.http.get<ShiftOption[]>(`${this.apiBaseUrl}/api/shifts`);
  }

  getProfiles(): Observable<ProfileOption[]> {
    return this.http.get<ProfileOption[]>(`${this.apiBaseUrl}/api/profiles`);
  }

  register(request: RegisterScrapRequest): Observable<ScrapSuccessResponse> {
    return this.http.post<ScrapSuccessResponse>(`${this.apiBaseUrl}/api/scrap`, request);
  }

  getScrapReport(shiftId: number): Observable<ScrapReportEntry[]> {
    return this.http.get<ScrapReportEntry[]>(`${this.apiBaseUrl}/api/scrap/report`, {
      params: { shiftId: shiftId.toString() },
    });
  }
}