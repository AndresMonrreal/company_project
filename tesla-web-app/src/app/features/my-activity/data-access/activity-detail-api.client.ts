import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../../../core/http/api-url.token';
import { ActivityAction } from '../models/activity-record.model';

export interface ReceptionDetail {
  type: 'RECEPTION';
  id: number;
  containerCode: string;
  profileCode: string;
  lot: string;
  receivedQuantity: number;
  status: string;
  receivedAt: string;
}

export interface CuttingDetail {
  type: 'CUT';
  id: number;
  containerCode: string;
  profileCode: string;
  machineCode: string;
  initialQuantity: number;
  goodQuantity: number;
  scrapQuantity: number;
  cutAt: string;
}

export interface ScrapDetail {
  type: 'SCRAP';
  id: number;
  cuttingRecordId: number;
  quantity: number;
  reason: string | null;
  createdAt: string;
}

export interface MoldingOutputDetail {
  type: 'MOLDING_OUTPUT';
  id: number;
  cuttingRecordId: number;
  quantitySent: number;
  sentAt: string;
}

export type ActivityDetail = ReceptionDetail | CuttingDetail | ScrapDetail | MoldingOutputDetail;

@Injectable({ providedIn: 'root' })
export class ActivityDetailApiClient {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getDetail(action: ActivityAction, id: number): Observable<ActivityDetail> {
    switch (action) {
      case 'RECEPTION':
        return this.http.get<Omit<ReceptionDetail, 'type'>>(`${this.apiBaseUrl}/api/receptions/${id}`).pipe(
          map(r => ({ ...r, type: 'RECEPTION' as const }))
        );
      case 'CUT':
        return this.http.get<Omit<CuttingDetail, 'type'>>(`${this.apiBaseUrl}/api/cutting/${id}`).pipe(
          map(r => ({ ...r, type: 'CUT' as const }))
        );
      case 'SCRAP':
        return this.http.get<Omit<ScrapDetail, 'type'>>(`${this.apiBaseUrl}/api/scrap/${id}`).pipe(
          map(r => ({ ...r, type: 'SCRAP' as const }))
        );
      case 'MOLDING_OUTPUT':
        return this.http.get<Omit<MoldingOutputDetail, 'type'>>(`${this.apiBaseUrl}/api/molding-outputs/${id}`).pipe(
          map(r => ({ ...r, type: 'MOLDING_OUTPUT' as const }))
        );
    }
  }
}
