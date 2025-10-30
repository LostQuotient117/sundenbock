import { Routes } from '@angular/router';
import { Health } from './features/health/health';
import { Tickets } from './features/tickets/ticket';
import { Projects } from './features/projects/projects';
import { TicketsPage } from './features/tickets/pages/ticket.page';

export const routes: Routes = [
     { path: 'health', component: Health },
     { path: 'tickets', component: TicketsPage },
     { path: 'projects', component: Projects },
  { path: '', pathMatch: 'full', redirectTo: 'tickets' }
];
