import { Routes } from '@angular/router';
import { Health } from './features/health/health';
import { Tickets } from './features/tickets/tickets';
import { Projects } from './features/projects/projects';

export const routes: Routes = [
     { path: 'health', component: Health },
     { path: 'tickets', component: Tickets },
     { path: 'projects', component: Projects },
  { path: '', pathMatch: 'full', redirectTo: 'health' }
];
