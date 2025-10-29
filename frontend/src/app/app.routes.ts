import { Routes } from '@angular/router';
import { Health } from './features/health/health';
import { Tickets } from './features/tickets/tickets';

export const routes: Routes = [
     { path: 'health', component: Health },
     { path: 'tickets', component: Tickets },
  { path: '', pathMatch: 'full', redirectTo: 'health' }
];
