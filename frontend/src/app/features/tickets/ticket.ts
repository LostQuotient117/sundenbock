import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketsService } from './ticket.service';

@Component({
  selector: 'app-tickets',
  imports: [CommonModule],
  templateUrl: './ticket.html',
  styleUrl: './ticket.css'
})
export class Tickets {
   tickets = signal<any[]>([]);
  loading = signal(false);

  constructor(private ticketsService: TicketsService) {
    this.loadTickets();
  }

  loadTickets() {
    this.loading.set(true);
    this.ticketsService.getTickets$().subscribe({
      next: (data) => this.tickets.set(data),
      error: () => this.tickets.set([]),
      complete: () => this.loading.set(false)
    });
  }
}
