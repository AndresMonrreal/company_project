export interface CuttingAvailableResult {
  cuttingRecordId: number;
  containerCode: string;
  profileCode: string;
  initialQuantity: number;
  goodQuantity: number;
  scrapQuantity: number;
  cutAt: string;
}

export interface RegisterMoldingOutputRequest {
  cuttingRecordId: number;
  quantitySent: number;
}

export interface MoldingOutputSuccessResponse {
  id: number;
  cuttingRecordId: number;
  quantitySent: number;
  operatorId: number;
  sentAt: string;
}
