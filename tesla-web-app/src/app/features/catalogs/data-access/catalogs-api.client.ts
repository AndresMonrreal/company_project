import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import { CatalogContainer, ContainerType, Machine, Profile, Shift } from '../models/catalogs.models';

@Injectable({ providedIn: 'root' })
export class CatalogsApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getProfiles(): Observable<Profile[]> {
    return this.http.get<Profile[]>(`${this.apiBaseUrl}/api/profiles`);
  }

  createProfile(body: { code: string; name: string; description?: string; type: string; position: string }): Observable<Profile> {
    return this.http.post<Profile>(`${this.apiBaseUrl}/api/profiles`, body);
  }

  updateProfile(id: number, body: { name: string; description?: string; type: string; position: string }): Observable<Profile> {
    return this.http.put<Profile>(`${this.apiBaseUrl}/api/profiles/${id}`, body);
  }

  deleteProfile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/profiles/${id}`);
  }

  getMachines(): Observable<Machine[]> {
    return this.http.get<Machine[]>(`${this.apiBaseUrl}/api/machines`);
  }

  createMachine(body: {
    name: string; code?: string | null; status: string; processesType: string;
    cycleTimeSeconds?: number | null; lastMaintenanceDate?: string | null; observations?: string | null;
  }): Observable<Machine> {
    return this.http.post<Machine>(`${this.apiBaseUrl}/api/machines`, body);
  }

  updateMachine(id: number, body: {
    name: string; code?: string | null; status: string; processesType?: string | null;
    cycleTimeSeconds?: number | null; lastMaintenanceDate?: string | null; observations?: string | null;
  }): Observable<Machine> {
    return this.http.put<Machine>(`${this.apiBaseUrl}/api/machines/${id}`, body);
  }

  deleteMachine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/machines/${id}`);
  }

  getShifts(): Observable<Shift[]> {
    return this.http.get<Shift[]>(`${this.apiBaseUrl}/api/shifts`);
  }

  createShift(body: { name: string; startTime: string; endTime: string }): Observable<Shift> {
    return this.http.post<Shift>(`${this.apiBaseUrl}/api/shifts`, body);
  }

  updateShift(id: number, body: { name: string; startTime: string; endTime: string }): Observable<Shift> {
    return this.http.put<Shift>(`${this.apiBaseUrl}/api/shifts/${id}`, body);
  }

  deleteShift(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/shifts/${id}`);
  }

  getContainerTypes(): Observable<ContainerType[]> {
    return this.http.get<ContainerType[]>(`${this.apiBaseUrl}/api/container-types`);
  }

  createContainerType(body: { name: string }): Observable<ContainerType> {
    return this.http.post<ContainerType>(`${this.apiBaseUrl}/api/container-types`, body);
  }

  updateContainerType(id: number, body: { name: string }): Observable<ContainerType> {
    return this.http.put<ContainerType>(`${this.apiBaseUrl}/api/container-types/${id}`, body);
  }

  deleteContainerType(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/container-types/${id}`);
  }

  getContainers(): Observable<CatalogContainer[]> {
    return this.http.get<CatalogContainer[]>(`${this.apiBaseUrl}/api/containers`);
  }

  createContainer(body: { containerTypeId: number; code: string }): Observable<CatalogContainer> {
    return this.http.post<CatalogContainer>(`${this.apiBaseUrl}/api/containers`, body);
  }

  updateContainer(id: number, body: { containerTypeId: number; code: string }): Observable<CatalogContainer> {
    return this.http.put<CatalogContainer>(`${this.apiBaseUrl}/api/containers/${id}`, body);
  }

  deleteContainer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/containers/${id}`);
  }
}