import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import {
  CutSuccessResponse,
  InventoryAvailableResult,
  MachineOption,
  RegisterCutRequest,
} from '../models/register-cut.models';
import { SKIP_AUTH_ERROR } from '../../../core/http/skip-auth-error.token';

@Injectable({ providedIn: 'root' })
export class RegisterCutApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getInventoryAvailable(lot: string): Observable<InventoryAvailableResult> {
    return this.http.get<InventoryAvailableResult>(`${this.apiBaseUrl}/api/inventory/available`, {
      params: { lot },
      context: new HttpContext().set(SKIP_AUTH_ERROR, true),
    });
  }

  getMachines(): Observable<MachineOption[]> {
    return this.http.get<MachineOption[]>(`${this.apiBaseUrl}/api/machines`);
  }

  register(request: RegisterCutRequest): Observable<CutSuccessResponse> {
    return this.http.post<CutSuccessResponse>(`${this.apiBaseUrl}/api/cutting`, request);
  }
}