// src/app/features/projects/projects.service.ts
import { Injectable, inject } from '@angular/core';
import { ApiService } from '../../core/http/api.service';
import { ResourceClient } from '../../core/http/resource-client';
import { Project } from './models/project';
import { HydratedProject } from './models/hydrated-project';
import { Page, PageQuery } from '../../shared/models/paging';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProjectsService extends ResourceClient<Project> {
  constructor() {
    super(inject(ApiService), '/projects'); // <-- passe den endpoint-Pfad an
  }

  // list liefert standardmäßig Page<Project>. Wir überschreiben, damit wir Datum-Felder "hydraten"
  override list(q?: PageQuery<Project>): Observable<Page<HydratedProject>> {
    return super.list(q).pipe(
      map((p: Page<Project>) => ({
        ...p,
        items: p.items.map(pr => this.hydrate(pr))
      }))
    );
  }

  // optional: getById mit Hydration
  override get(id: number): Observable<HydratedProject> {
    return super.get(id).pipe(map(pr => this.hydrate(pr)));
  }

  // helper zur Hydration (sichere Parsers)
  private hydrate(p: Project): HydratedProject {
    return {
      ...p,
      createdDate: p.createdDate ? new Date(p.createdDate) : new Date(0),
      lastModifiedDate: p.lastModifiedDate ? new Date(p.lastModifiedDate) : new Date(0)
    };
  }
}
