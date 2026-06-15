export interface ContainerOption {
  id: number;
  code: string;
}

export interface ProfileOption {
  id: number;
  code: string;
}

export interface RegisterReceptionRequest {
  containerId: number;
  profileId: number;
  lot: string;
  receivedQuantity: number;
}

export interface ReceptionSuccessResponse {
  id: number;
  containerCode: string;
  profileCode: string;
  lot: string;
  receivedQuantity: number;
  status: string;
  receivedAt: string;
}
