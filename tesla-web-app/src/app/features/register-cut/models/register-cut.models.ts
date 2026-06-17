export interface InventoryAvailableResult {
  inventoryItemId: number;
  containerCode: string;
  profileCode: string;
  lot: string;
  availableQuantity: number;
}

export interface MachineOption {
  id: number;
  name: string;
}

export interface RegisterCutRequest {
  inventoryItemId: number;
  machineId: number;
  shiftId: number;
  initialQuantity: number;
  goodQuantity: number;
  scrapQuantity: number;
}

export interface CutSuccessResponse {
  id: number;
  containerCode: string;
  profileCode: string;
  machineName: string;
  initialQuantity: number;
  goodQuantity: number;
  scrapQuantity: number;
  cutAt: string;
}