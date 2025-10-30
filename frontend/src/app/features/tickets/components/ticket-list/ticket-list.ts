import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HydratedTicket } from '../../../../shared/models/types';

@Component({
  selector: 'app-ticket-list',
  imports: [CommonModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css'
})
export class TicketList {
  @Input({ required: true }) tickets: HydratedTicket[] = [];
}
