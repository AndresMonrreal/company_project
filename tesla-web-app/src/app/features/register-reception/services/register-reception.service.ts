import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RegisterReceptionApiClient } from '../data-access/register-reception-api.client';
import {
  ContainerOption,
  ProfileOption,
  RegisterReceptionRequest,
  ReceptionSuccessResponse,
} from '../models/register-reception.models';

@Injectable({ providedIn: 'root' })
export class RegisterReceptionService {
  private readonly apiClient = inject(RegisterReceptionApiClient);

  loadContainers(): Observable<ContainerOption[]> {
    return this.apiClient.getContainers();
  }

  loadProfiles(): Observable<ProfileOption[]> {
    return this.apiClient.getProfiles();
  }

  register(request: RegisterReceptionRequest): Observable<ReceptionSuccessResponse> {
    return this.apiClient.registerReception(request);
  }
}
