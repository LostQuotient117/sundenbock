import { CommonModule, DatePipe } from '@angular/common';
import { Component, computed, inject, signal, Signal } from '@angular/core';
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
  private projectLoaded: Signal<Project | undefined> = toSignal(
    this.route.paramMap.pipe(
      switchMap((pm) => this.svc.get(pm.get('id')!))
    ),
    { initialValue: undefined }
  );

  //Projekt Override nach Update
  private projectOverride = signal<Project | undefined>(undefined);

  // View-Signal
  project: Signal<Project | undefined> = computed(
    () => this.projectOverride() ?? this.projectLoaded()
  );

  // Editier State
  editing = signal(false);
  saving  = signal(false);
  saveError = signal<string | null>(null);
  form = { title: '', abbreviation: '', description: '' };

  startEdit() {
    const p = this.project();
    if (!p) return;
    this.form = {
      title: p.title ?? '',
      abbreviation: p.abbreviation ?? '',
      description: p.description ?? '',
    };
    this.saveError.set(null);
    this.editing.set(true);
  }

  cancelEdit() {
    if (this.saving()) return;
    this.editing.set(false);
    this.saveError.set(null);
  }

  submitUpdate() {
    const p = this.project();
    if (!p || this.saving()) return;

    const { title, abbreviation, description } = this.form;
    if (!title || !description || !abbreviation || abbreviation.length !== 3) {
      this.saveError.set('Bitte alle Felder korrekt ausfüllen (Kürzel = 3 Zeichen).');
      return;
    }

    this.saving.set(true);
    this.saveError.set(null);

    this.svc.update(p.id, { title, abbreviation, description }).subscribe({
      next: updated => {
        this.saving.set(false);
        this.editing.set(false);
        this.projectOverride.set(updated); // **ohne Reload** sofort sichtbar
      },
      error: err => {
        this.saving.set(false);
        const msg =
          err?.status === 403
            ? 'Keine Berechtigung für diese Aktion.'
            : (err?.error?.message ?? 'Aktualisierung fehlgeschlagen.');
        this.saveError.set(msg);
      }
    });
  }

  // Nutzeranzeige
  userDisplay = (u?: Project['createdBy']) => {
    if (!u) return '—';
    const full = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
    return full || u.username || (u.id != null ? `#${u.id}` : '—');
  };
}
