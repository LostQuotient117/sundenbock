import { Component, signal, computed } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { combineLatest, debounceTime, switchMap } from 'rxjs';
import { ProjectsService } from './projects.service';
import { ProjectList } from './components/project-list/project-list';
import { Page, SortKey } from '@shared/models/paging';
import { HydratedProject } from './models/hydrated-project';
import { Project } from './models/project';

@Component({
  standalone: true,
  selector: 'projects-page',
  imports: [ProjectList],
  templateUrl: './projects.page.html',
})
export class ProjectsPage {
  search = signal('');
  page = signal(0);
  size = signal(20);
  sort = signal<SortKey<Project>>('createdDate:desc'); 

  constructor(private svc: ProjectsService) {}

  private query$ = combineLatest([
    toObservable(this.search).pipe(debounceTime(300)),
    toObservable(this.page),
    toObservable(this.size),
    toObservable(this.sort),
  ]).pipe(
    switchMap(([search, page, size, sort]) =>
      this.svc.list({ search, page,  pageSize:size, sort })
    )
  );

  vm = toSignal(this.query$);

  onSearchInput(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.search.set(value);
  }
}
