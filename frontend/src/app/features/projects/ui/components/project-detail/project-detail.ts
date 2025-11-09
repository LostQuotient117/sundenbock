import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject, Signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Project } from '@features/projects/domain/project.model';
import { ProjectsService } from '@features/projects/domain/projects.service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './project-detail.html',
})
export class ProjectDetail {
  private route = inject(ActivatedRoute);
  private svc = inject(ProjectsService);

  // Projekt laden
  project: Signal<Project | undefined> = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => this.svc.get(pm.get('id')!))
    ),
    { initialValue: undefined }
  );

  // Nutzeranzeige
  userDisplay = (u?: Project['createdBy']) => {
    if (!u) return '—';
    const full = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
    return full || u.username || (u.id != null ? `#${u.id}` : '—');
  };
}
