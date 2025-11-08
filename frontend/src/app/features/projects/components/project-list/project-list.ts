import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HydratedProject } from '../../models/hydrated-project';

@Component({
  standalone: true,
  selector: 'app-project-list',
  imports: [CommonModule, RouterModule],
  templateUrl: './project-list.html',
})
export class ProjectList {
  @Input({ required: true }) projects: HydratedProject[] = [];

  // Optional: wenn ihr einen Projektstatus habt (z. B. ACTIVE/ARCHIVED/ON_HOLD …)
  statusLabelMap: Record<string, string> = {
    ACTIVE: 'Active',
    ON_HOLD: 'On Hold',
    ARCHIVED: 'Archived'
  };
}
