import { Routes } from '@angular/router';
import { AppLayout } from './shared/components/layout/layout';
import { authChildGuard, authMatchGuard } from '@core/auth/auth.guard';

export const routes: Routes = [
  // Öffentliche Routen
  {
    path: 'auth',
    loadComponent: () => import('./features/auth/ui/auth-shell/auth-shell').then(m => m.AuthShell),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/ui/login/login').then(m => m.Login)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/ui//register/register').then(m => m.RegisterComponent)
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
        loadComponent: () => import('./features/tickets/ui/pages/ticket.page').then(m => m.TicketsPage)
      },
      {
        path: 'tickets/:id',
        loadComponent: () => import('./features/tickets/ui/components/ticket-detail/ticket-detail').then(m => m.TicketDetail)
      },
      {
        path: 'projects',
        loadComponent: () => import('./features/projects/ui/pages/project.page').then(m => m.ProjectsPage)
      },
      {
      path: 'projects/:id',
      loadComponent: () => import('./features/projects/ui/components/project-detail/project-detail').then(m => m.ProjectDetail)
    },
      { path: '', pathMatch: 'full', redirectTo: 'tickets' }
    ]
  },

  // Fallback (404 → Root)
  { path: '**', redirectTo: '' }
];
