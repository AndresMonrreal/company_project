import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import {
  ContainerOption,
  ProfileOption,
  RegisterReceptionRequest,
  ReceptionSuccessResponse,
} from '../models/register-reception.models';

@Injectable({ providedIn: 'root' })
export class RegisterReceptionApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getContainers(): Observable<ContainerOption[]> {
    return this.http.get<ContainerOption[]>(`${this.apiBaseUrl}/api/containers`);
  }

  getProfiles(): Observable<ProfileOption[]> {
    return this.http.get<ProfileOption[]>(`${this.apiBaseUrl}/api/profiles`);
  }

  register(request: RegisterReceptionRequest): Observable<ReceptionSuccessResponse> {
    return this.http.post<ReceptionSuccessResponse>(`${this.apiBaseUrl}/api/receptions`, request);
  }
}