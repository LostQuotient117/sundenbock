// app/features/projects/ui/pages/projects.page.ts
import { Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ProjectsService } from '@features/projects/domain/projects.service';
import { Project } from '@features/projects/domain/project.model';
import { ProjectDto } from '@features/projects/data/project.dto';
import { Page, PageQuery, SortDir, SortKey } from '@shared/models/paging';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProjectListComponent } from '@features/projects/ui/components/project-list/project-list';

@Component({
  standalone: true,
  selector: 'projects-page',
  imports: [CommonModule, FormsModule, ProjectListComponent],
  templateUrl: './project.page.html', // <- Dateiname passt exakt zur HTML unten
})
export class ProjectsPage {
  private svc = inject(ProjectsService);
  private destroyRef = inject(DestroyRef);

  // Query-State (Signals)
  search = signal<string>('');
  page = signal<number>(0);
  pageSize = signal<number>(20);
  sort = signal<SortKey<ProjectDto> | undefined>(undefined);

  // Ergebnis
  result = signal<Page<Project>>({
    items: [],
    total: 0,
    page: 0,
    pageSize: this.pageSize(),
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.result().total / this.pageSize())));

  private query = computed<PageQuery<ProjectDto>>(() => ({
    search: this.search() || undefined,
    page: this.page(),
    pageSize: this.pageSize(),
    sort: this.sort(),
  }));

  constructor() {
    effect(() => {
      const q = this.query();
      this.svc.list(q)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(p => this.result.set(p));
    });
  }

  // UI-Aktionen
  onSearchEnter() { this.page.set(0); }
  clearSearch()   { this.search.set(''); this.page.set(0); }
  setPageSize(size: number) { this.pageSize.set(size); this.page.set(0); }

  prev() { this.page.update(p => Math.max(0, p - 1)); }
  next() {
    const last = this.totalPages() - 1;
    this.page.update(p => Math.min(last, p + 1));
  }

  setSort<K extends Extract<keyof ProjectDto, string>>(key: K, dir: SortDir) {
    if (key === 'createdBy') {
      // createdBy.username erstmal bewusst deaktiviert:
      this.sort.set(undefined);
      return;
    }
    this.sort.set(`${key}:${dir}` as SortKey<ProjectDto>);
    this.page.set(0);
  }

  // Convenience-Handler
  sortByCreatedAsc()  { this.setSort('createdDate', 'asc'); }
  sortByCreatedDesc() { this.setSort('createdDate', 'desc'); }
  sortByTitleAsc()    { this.setSort('title', 'asc'); }
  sortByTitleDesc()   { this.setSort('title', 'desc'); }

  // Create Project Logik
  showCreate = signal(false);
  creating   = signal(false);
  createError = signal<string | null>(null);

  createModel = {
    title: '',
    abbreviation: '',
    description: '',
  };

  openCreate() {
    this.createModel = { title: '', abbreviation: '', description: '' };
    this.createError.set(null);
    this.showCreate.set(true);
  }

  closeCreate() {
    if (this.creating()) return;
    this.showCreate.set(false);
  }

  submitCreate() {
    // Validierung
    const { title, abbreviation, description } = this.createModel;
    if (!title || !description || !abbreviation || abbreviation.length !== 3) return;

    this.creating.set(true);
    this.createError.set(null);

    this.svc.create({
      title,
      description,
      abbreviation,
    }).subscribe({
      next: () => {
        this.creating.set(false);
        this.showCreate.set(false);
        // Liste neu laden nach Anlage
        this.page.set(this.page() + 1);
        this.page.set(0);
      },
      error: (err) => {
        this.creating.set(false);
const msg =
    err?.status === 403
      ? 'Keine Berechtigung für diese Aktion.'
      : (err?.error?.message ?? 'Anlage fehlgeschlagen.');

      this.createError.set(msg);      }
    });
  }
}
