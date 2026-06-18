export interface Profile {
  id: number;
  code: string;
  name: string;
  description: string | null;
  active: boolean;
}

export interface Machine {
  id: number;
  name: string;
  active: boolean;
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