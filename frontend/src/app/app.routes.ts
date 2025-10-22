import { Routes } from '@angular/router';
import { Health } from './features/health/health';

export const routes: Routes = [
     { path: 'health', component: Health },
  { path: '', pathMatch: 'full', redirectTo: 'health' }
];
