// app/features/projects/domain/projects.service.ts
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ProjectsClient } from '../data/projects.client';
import { Project } from './project.model';
import { ProjectDto } from '../data/project.dto';
import { Page, PageQuery } from '@shared/models/paging';
import { mapPage } from '@shared/utils/mapping.base';
import { mapProject } from './project.mapper';

@Injectable({ providedIn: 'root' })
export class ProjectsService {
  private api = inject(ProjectsClient);

  list(q?: PageQuery<ProjectDto>): Observable<Page<Project>> {
    return this.api.list(q).pipe(map(p => mapPage(p, mapProject)));
  }

  get(id: number | string): Observable<Project> {
    return this.api.get(id).pipe(map(mapProject));
  }

  create(payload: Partial<ProjectDto>): Observable<Project> {
    // Backend-Endpoint ist POST /api/v1/projects/create (laut Controller)
    // ResourceClient.create() postet auf '/projects' – falls dein Backend zwingend '/create' erwartet,
    // kannst du hier einen spezialisierten Call über ApiService bauen.
    return this.api.create(payload as ProjectDto).pipe(map(mapProject));
  }

  update(id: number | string, payload: Partial<ProjectDto>): Observable<Project> {
    // analog Hinweis wie bei create() bzgl. '/{id}/update'
    return this.api.update(id, payload as ProjectDto).pipe(map(mapProject));
  }

  delete(id: number | string): Observable<void> {
    return this.api.delete(id);
  }
}
