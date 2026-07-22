export type ProfileType = 'HEADER' | 'LOWER';
export type ProfilePosition = 'FRONT' | 'REAR';
export type MachineStatus = 'OPERATIONAL' | 'MAINTENANCE' | 'OUT_OF_SERVICE';
export type ProcessesType = 'HEADER' | 'LOWER' | 'BOTH';

export interface Profile {
  id: number;
  code: string;
  name: string;
  description: string | null;
  active: boolean;
  type: ProfileType | null;
  position: ProfilePosition | null;
}

export interface Machine {
  id: number;
  name: string;
  active: boolean;
  code: string | null;
  status: MachineStatus | null;
  processesType: ProcessesType | null;
  cycleTimeSeconds: number | null;
  lastMaintenanceDate: string | null;
  observations: string | null;
}

export interface Shift {
  id: number;
  name: string;
  startTime: string;
  endTime: string;
  active: boolean;
}

export interface ContainerType {
  id: number;
  name: string;
  active: boolean;
}

export interface CatalogContainer {
  id: number;
  containerTypeId: number;
  code: string;
  active: boolean;
}