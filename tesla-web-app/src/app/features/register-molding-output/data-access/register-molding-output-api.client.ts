import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import {
  CuttingAvailableResult,
  MoldingOutputSuccessResponse,
  RegisterMoldingOutputRequest,
} from '../models/register-molding-output.models';
import { SKIP_AUTH_ERROR } from '../../../core/http/skip-auth-error.token';

@Injectable({ providedIn: 'root' })
export class RegisterMoldingOutputApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getCuttingAvailable(containerCode: string): Observable<CuttingAvailableResult> {
    return this.http.get<CuttingAvailableResult>(`${this.apiBaseUrl}/api/cutting/available`, {
      params: { containerCode },
      context: new HttpContext().set(SKIP_AUTH_ERROR, true),
    });
  }

  register(request: RegisterMoldingOutputRequest): Observable<MoldingOutputSuccessResponse> {
    return this.http.post<MoldingOutputSuccessResponse>(`${this.apiBaseUrl}/api/molding-outputs`, request);
  }
}