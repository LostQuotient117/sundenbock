import { Routes } from '@angular/router';
import { AppLayout } from './shared/components/layout/layout';
import { authChildGuard, authMatchGuard } from '@core/auth/auth.guard';

export const routes: Routes = [
  // Öffentliche Routen
  {
    path: 'auth',
    loadComponent: () => import('./features/auth/auth-shell/auth-shell').then(m => m.AuthShell),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register').then(m => m.RegisterComponent)
      },
      { path: '', pathMatch: 'full', redirectTo: 'login' } // Standard: /auth → /auth/login
    ]
  },

  // Geschützte Routen
  {
    path: '',
    component: AppLayout,
    canMatch: [authMatchGuard],
    canActivateChild: [authChildGuard],
    children: [
      {
        path: 'tickets',
        loadComponent: () => import('./features/tickets/pages/ticket.page').then(m => m.TicketsPage)
      },
      {
        path: 'projects',
        loadComponent: () => import('./features/projects/projects').then(m => m.Projects)
      },
      {
        path: 'health',
        loadComponent: () => import('./features/health/health').then(m => m.Health)
      },
      { path: '', pathMatch: 'full', redirectTo: 'tickets' }
    ]
  },

  // Fallback (404 → Root)
  { path: '**', redirectTo: '' }
];
