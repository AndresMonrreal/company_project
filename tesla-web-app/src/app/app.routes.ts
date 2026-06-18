import { Routes } from '@angular/router';
import { AppShellComponent } from './core/layout/app-shell.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPageComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    component: AppShellComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.page').then((m) => m.DashboardPageComponent),
      },
      {
        path: 'my-activity',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'] },
        loadComponent: () => import('./features/my-activity/pages/my-activity.page').then((m) => m.MyActivityPageComponent),
      },
      {
        path: 'register-reception',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'] },
        loadComponent: () =>
          import('./features/register-reception/pages/register-reception.page').then(
            (m) => m.RegisterReceptionPageComponent
          ),
      },
      {
        path: 'register-cut',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'] },
        loadComponent: () =>
          import('./features/register-cut/pages/register-cut.page').then(
            (m) => m.RegisterCutPageComponent
          ),
      },
      {
        path: 'register-molding-output',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'SUPERVISOR', 'OPERADOR'] },
        loadComponent: () =>
          import('./features/register-molding-output/pages/register-molding-output.page').then(
            (m) => m.RegisterMoldingOutputPageComponent
          ),
      },
      {
        path: 'catalogs',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () => import('./features/catalogs/pages/catalogs.page').then((m) => m.CatalogsPageComponent),
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'SUPERVISOR', 'CONSULTA'] },
        loadComponent: () => import('./features/reports/pages/reports.page').then((m) => m.ReportsPageComponent),
      },
      {
        path: 'coming-soon',
        loadComponent: () => import('./shared/ui/coming-soon.component').then((m) => m.ComingSoonComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: '**', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
