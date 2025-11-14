import { Component, computed, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Ticket, TicketStatus } from '@features/tickets/domain/ticket.model';
import { TicketStatusLabelPipe } from '@shared/pipes/status-label.pipe';
import { TicketsService } from '@features/tickets/domain/ticket.service';
import { AuthService } from '@core/auth/auth.service';

@Component({
  standalone: true,
  selector: 'app-ticket-list',
  imports: [CommonModule, RouterModule, TicketStatusLabelPipe],
  templateUrl: './ticket-list.html',
})
export class TicketList {
  private svc = inject(TicketsService);
  private authService = inject(AuthService);
  
  @Input({ required: true }) tickets: Ticket[] = [];
  @Output() deleted = new EventEmitter<void>();

  isAdmin = computed(() => this.authService.hasRole('ROLE_ADMIN'));

  readonly allStatuses = Object.values(TicketStatus);

  badge: Record<Ticket['status'], string> = {
    [TicketStatus.CREATED]: 'badge-info',
    [TicketStatus.REOPENED]: 'badge-info',
    [TicketStatus.IN_PROGRESS]: 'badge-warning',
    [TicketStatus.RESOLVED]: 'badge-success',
    [TicketStatus.REJECTED]: 'badge-error',
    [TicketStatus.CLOSED]: 'badge-neutral',
  };

  // delete pop up
  toDelete = signal<Ticket | null>(null);
  deleting = signal(false);
  deleteError = signal<string | null>(null);

  confirmDelete(t: Ticket) {
    this.toDelete.set(t);
    this.deleteError.set(null);
  }
  
  cancelDelete() {
    if (this.deleting()) return;
    this.toDelete.set(null);
    this.deleteError.set(null);
  }

  deleteConfirmed() {
    const t = this.toDelete();
    if (!t || this.deleting()) return;

    this.deleting.set(true);
    this.deleteError.set(null);

    this.svc.delete(t.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.deleted.emit();
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
