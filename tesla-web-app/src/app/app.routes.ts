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
        loadComponent: () => import('./features/register-reception/pages/register-reception.page').then((m) => m.RegisterReceptionPageComponent),
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
