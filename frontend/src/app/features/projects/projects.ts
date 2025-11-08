import { CommonModule } from '@angular/common';
import { Component, OnDestroy, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import { ProjectsService } from './projects.service';
import { HydratedProject } from './models/hydrated-project';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './projects.html',
})
export class Projects implements OnDestroy {
  projects = signal<HydratedProject[]>([]);
  loading = signal(false);
  private sub = new Subscription();

  constructor(private projectsService: ProjectsService) {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading.set(true);
    const s = this.projectsService.list().subscribe({
      next: page => this.projects.set(page.items),
      error: () => this.projects.set([]),
      complete: () => this.loading.set(false)
    });
    this.sub.add(s);
  }

  trackById(_: number, p: HydratedProject) {
    return p.id;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
