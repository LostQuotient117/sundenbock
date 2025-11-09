import { Component, Input, inject, signal, Signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, switchMap, map } from 'rxjs/operators';
import { combineLatest } from 'rxjs';

import { ProjectsService } from '@features/projects/domain/projects.service';
import { Page, PageQuery } from '@shared/models/paging';
import { Project } from '@features/projects/domain/project.model';
import { ProjectDto } from '@features/projects/data/project.dto';

@Component({
  standalone: true,
  selector: 'project-select',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './project-select.html',
})
export class ProjectSelectComponent {
  private svc = inject(ProjectsService);

  // FormControl<number> vom Parent
  @Input({ required: true }) control!: FormControl<number>;
  @Input() placeholder = 'Projekt auswählen…';

  // UI-State
  open = signal(false);
  search = signal('');
  page = signal(0);
  readonly size = 10;

  activeIndex = signal<number>(-1);

  // Ergebnisliste
  vm: Signal<Page<Project>> = toSignal(
    combineLatest([
      toObservable(this.search).pipe(debounceTime(250)),
      toObservable(this.page),
    ]).pipe(
      map(([search, page]) => {
        const q: PageQuery<ProjectDto> = {
          search: search || undefined,
          page,
          pageSize: this.size,
          sort: 'createdDate:desc',
        };
        return q;
      }),
      switchMap(q => this.svc.list(q))
    ),
    { initialValue: { items: [], total: 0, page: 0, pageSize: this.size } }
  );

  //öffnen und sxchließen
  toggle() { this.open.update(v => !v); this.activeIndex.set(-1); }
  openPanel()  { this.open.set(true);  this.activeIndex.set(-1); }
  closePanel() { this.open.set(false); this.activeIndex.set(-1); }

  onInput(e: Event) {
    const el = e.target as HTMLInputElement;
    this.search.set(el.value ?? '');
    this.page.set(0);
    if (!this.open()) this.openPanel();
  }

  choose(p: Project) {
    // schreibt id ins Control
    this.control.setValue(Number(p.id));
    // Suchtext auf Titel für UI
    this.search.set(p.title);
    this.closePanel();
  }

  prev() {
    const newPage = Math.max(0, this.page() - 1);
    this.page.set(newPage);
    this.activeIndex.set(-1);
  }

  next() {
    const totalPages = this.totalPages(this.vm());
    const newPage = Math.min(totalPages - 1, this.page() + 1);
    this.page.set(newPage);
    this.activeIndex.set(-1);
  }

  totalPages(page: Page<Project>): number {
    return Math.max(1, Math.ceil(page.total / page.pageSize));
  }

  // Tastatur im Input: Pfeile/Enter/Escape
  onKeydown(e: KeyboardEvent) {
    const items = this.vm().items;
    if (!this.open() && (e.key === 'ArrowDown' || e.key === 'ArrowUp')) {
      this.openPanel();
      e.preventDefault();
      return;
    }
    if (!this.open()) return;

    if (e.key === 'ArrowDown') {
      this.activeIndex.update(i => Math.min(items.length - 1, i + 1));
      e.preventDefault();
    } else if (e.key === 'ArrowUp') {
      this.activeIndex.update(i => Math.max(0, i - 1));
      e.preventDefault();
    } else if (e.key === 'Enter') {
      const i = this.activeIndex();
      if (i >= 0 && i < items.length) this.choose(items[i]);
      e.preventDefault();
    } else if (e.key === 'Escape') {
      this.closePanel();
      e.preventDefault();
    }
  }

  // Klick außerhalb schließt Panel
  @HostListener('document:click', ['$event'])
  onDocClick(ev: MouseEvent) {
    const target = ev.target as HTMLElement | null;
    if (!target?.closest('[data-role="project-select-root"]')) {
      this.closePanel();
    }
  }

}
