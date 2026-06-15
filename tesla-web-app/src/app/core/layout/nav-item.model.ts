export interface NavItem {
  label: string;
  route: string;
  roles: Array<'ADMIN' | 'SUPERVISOR' | 'OPERADOR' | 'CONSULTA'>;
}

export const NAV_ITEMS: NavItem[] = [
  {
    label: 'Dashboard',
    route: '/dashboard',
    roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR', 'CONSULTA'],
  },
  {
    label: 'My Activity',
    route: '/my-activity',
    roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'],
  },
  {
    label: 'Register Reception',
    route: '/register-reception',
    roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'],
  },
  {
    label: 'Register Cut',
    route: '/coming-soon',
    roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'],
  },
  {
    label: 'Register Molding Output',
    route: '/coming-soon',
    roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'],
  },
  {
    label: 'Reports',
    route: '/coming-soon',
    roles: ['ADMIN', 'SUPERVISOR', 'CONSULTA'],
  },
  {
    label: 'Catalogs',
    route: '/coming-soon',
    roles: ['ADMIN'],
  },
];
