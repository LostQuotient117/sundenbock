import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { ProjectsService } from './projects.service';

@Component({
  selector: 'app-projects',
  imports: [CommonModule],
  templateUrl: './projects.html',
})
export class Projects {
  projects = signal<any[]>([]);
  loading = signal(false);

  constructor(private projectsService: ProjectsService) {
    this.loadProjects();
  }

  loadProjects() {
    this.loading.set(true);
    this.projectsService.getProjects$().subscribe({
      next: (data) => this.projects.set(data),
      error: () => this.projects.set([]),
      complete: () => this.loading.set(false)
    });
  }
}
