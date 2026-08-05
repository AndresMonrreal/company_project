export interface ShiftOption {
  id: number;
  name: string;
  active: boolean;
}

export interface ProfileOption {
  id: number;
  code: string;
  name: string;
  active: boolean;
}

export interface RegisterScrapRequest {
  shiftId: number;
  profileId: number;
  quantity: number;
  reason: string | null;
}

export interface ScrapSuccessResponse {
  id: number;
  shiftId: number;
  shiftName: string;
  profileId: number;
  profileCode: string;
  operatorId: number;
  quantity: number;
  reason: string | null;
  createdAt: string;
}

export interface ScrapReportEntry {
  id: number;
  time: string;
  source: 'CUTTING' | 'MOLDING';
  profileCode: string;
  machineName: string | null;
  lot: string | null;
  quantity: number;
  reason: string | null;
}