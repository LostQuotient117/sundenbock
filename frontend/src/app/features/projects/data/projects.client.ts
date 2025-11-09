// app/features/projects/data/projects.client.ts
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '@core/http/api.service';
import { ResourceClient } from '@core/http/resource-client';
import { Page, PageQuery } from '@shared/models/paging';
import { ProjectDto, CreateProjectDTO } from './project.dto';
import { HttpContext } from '@angular/common/http';
import { SUPPRESS_403_REDIRECT } from '@core/http/http-context';

@Injectable({ providedIn: 'root' })
export class ProjectsClient extends ResourceClient<ProjectDto> {
  constructor() {
    const api = inject(ApiService);
    super(api, '/projects'); // ergibt {apiBase}/projects -> z.B. /api/v1/projects
  }

  override list(q?: PageQuery<ProjectDto>): Observable<Page<ProjectDto>> {
    return super.list(q);
  }

  createProject(body: CreateProjectDTO): Observable<ProjectDto> {
    //401 context suppress
    const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
    return this.api.post<ProjectDto>('/projects/create', body, undefined, { context: ctx });
  }

  deleteProject(id: number | string): Observable<void> {
    const ctx = new HttpContext().set(SUPPRESS_403_REDIRECT, true);
    return this.api.delete<void>(`/projects/${id}/delete`, undefined, { context: ctx });
  }
}
