export type ActivityAction = 'RECEPTION' | 'CUT' | 'SCRAP' | 'MOLDING_OUTPUT';
export type ActivityStatus = 'RECEIVED' | 'CUT' | 'CLOSED' | 'SENT_TO_MOLDING' | 'IN_CUTTING';

export interface ActivityRecord {
  id: number;
  time: string;
  containerCode: string;
  profileCode: string;
  action: ActivityAction;
  quantities: string;
  status: ActivityStatus | null;
}

export const ACTION_BADGE_COLOR: Record<ActivityAction, string> = {
  RECEPTION: 'bg-blue-100 text-blue-800',
  CUT: 'bg-yellow-100 text-yellow-800',
  SCRAP: 'bg-orange-100 text-orange-800',
  MOLDING_OUTPUT: 'bg-purple-100 text-purple-800',
};

export const ACTION_LABEL: Record<ActivityAction, string> = {
  RECEPTION: 'Reception',
  CUT: 'Cut',
  SCRAP: 'Scrap',
  MOLDING_OUTPUT: 'Molding Output',
};

export function statusBadgeColor(status: ActivityStatus | null): string {
  if (!status) return '';
  return STATUS_BADGE_COLOR_MAP[status] ?? '';
}

const STATUS_BADGE_COLOR_MAP: Record<ActivityStatus, string> = {
  RECEIVED: 'bg-blue-100 text-blue-800',
  IN_CUTTING: 'bg-yellow-100 text-yellow-800',
  CUT: 'bg-green-100 text-green-800',
  SENT_TO_MOLDING: 'bg-purple-100 text-purple-800',
  CLOSED: 'bg-gray-100 text-gray-600',
};
