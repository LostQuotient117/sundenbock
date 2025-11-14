/**
 * @file project-list.ts
 *
 * Logik für die ProjectListComponent.
 * Diese Komponente zeigt die übergebene Projektliste an (`@Input items`).
 * Sie verwaltet den Lösch-Dialog und ruft den ProjectsService auf.
 * Meldet erfolgreiche Löschungen über `@Output deleted` an den Parent.
 */
import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Project } from '@features/projects/domain/project.model';
import { ProjectsService } from '@features/projects/domain/projects.service';

@Component({
  standalone: true,
  selector: 'project-list',
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './project-list.html',
})
export class ProjectListComponent {
  private svc = inject(ProjectsService);
  
  @Input({ required: true }) items: Project[] = [];
  //neu laden des Parent
  @Output() deleted = new EventEmitter<void>();

  // delete funktion für projekte
  toDelete = signal<Project | null>(null);
  deleting = signal(false);
  deleteError = signal<string | null>(null);

  confirmDelete(p: Project) {
    this.toDelete.set(p);
    this.deleteError.set(null);
  }

  cancelDelete() {
    if (this.deleting()) return;
    this.toDelete.set(null);
    this.deleteError.set(null);
  }

  deleteConfirmed() {
    const p = this.toDelete();
    if (!p || this.deleting()) return;

    this.deleting.set(true);
    this.deleteError.set(null);

    this.svc.delete(p.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.deleted.emit(); // → Parent triggert Reload
      },
      error: (err) => {
        this.deleting.set(false);
        const msg =
        err?.status === 403
        ? 'Keine Berechtigung für diese Aktion.'
        : (err?.error?.message ?? 'Löschen fehlgeschlagen.');
        this.deleteError.set(msg);
      },
    });
  }
}
