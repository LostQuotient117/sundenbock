import { Component, inject  } from '@angular/core';
import { ActivatedRoute, RouterLinkWithHref, RouterModule } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs/operators';
import { TicketsService } from '../../ticket.service';
import { HydratedTicket } from '../../../../shared/models/types';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-ticket-detail',
  imports: [CommonModule, RouterModule],
  templateUrl: './ticket-detail.html',
})
export class TicketDetail {
  private route = inject(ActivatedRoute);
  private svc = inject(TicketsService);

  ticket = toSignal(
    this.route.paramMap.pipe(
      switchMap(params => this.svc.get(params.get('id')!))
    ),
    { initialValue: null as unknown as HydratedTicket }
  );
}
