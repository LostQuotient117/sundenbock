import { defineMapper, mapBySpec, OutputFromSpec } from '@shared/utils/mapping/mapping.dsl';
import { TicketDto } from '../data/ticket.dto';
import { Ticket } from './ticket.model';

// 1) Spec definieren: Zielkeys -> Regel
const ticketSpec = defineMapper<TicketDto, Ticket>()({
  id:         { kind: 'keep' },
  title:      { kind: 'keep' },
  status:     { kind: 'keep' },
  assigneeId: { kind: 'keep' },

  // String -> Date
  createdAt:  { kind: 'map', map: (s: string) => new Date(s) },
  updatedAt:  { kind: 'map', map: (s: string) => new Date(s) },
}as const);

// 2) Abgeleiteter Typ (optional, zu Doku/Tests)
type TicketFromSpec = OutputFromSpec<TicketDto, typeof ticketSpec>;  // == Ticket

// 3) Öffentliche Mapper-Funktion (so nutzt der Rest der App den Mapper)
export function mapTicket(dto: TicketDto): Ticket {
  return mapBySpec(dto, ticketSpec);
}
