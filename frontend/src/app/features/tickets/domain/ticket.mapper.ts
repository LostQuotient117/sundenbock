import { defineMapper, mapBySpec, OutputFromSpec } from '@shared/utils/mapping/mapping.dsl';
import { TicketDto, TicketResponsiblePersonDto } from '../data/ticket.dto';
import { Ticket } from './ticket.model';

// 1) Spec definieren: Zielkeys -> Regel
const ticketSpec = defineMapper<TicketDto, Ticket>()({
  id:         { kind: 'keep' },
  title:      { kind: 'keep' },
  description: { kind: 'keep' },
  status:     { kind: 'keep' },
  assigneeId: { kind: 'keep' },
  ticketKey:  { kind: 'keep' },
  responsiblePersonUserName: { kind: 'keep' },
  responsiblePerson: {
    kind: 'map',
    from: 'responsiblePerson',
    map: (rp: TicketResponsiblePersonDto | undefined) => rp
      ? { id: rp.id, username: rp.username, firstName: rp.firstName, lastName: rp.lastName }
      : undefined
  },
  project: {
    kind: 'map',
    from: 'project',
    map: (p: any) => p ? { id: p.id, title: p.title, abbreviation: p.abbreviation } : undefined,
  },
  // String -> Date
  createdDate:  { kind: 'map', map: (s: string) => new Date(s) },
  lastModifiedDate:  { kind: 'map', map: (s: string) => new Date(s) },
}as const);

// 2) Abgeleiteter Typ (optional, zu Doku/Tests)
type TicketFromSpec = OutputFromSpec<TicketDto, typeof ticketSpec>;  // == Ticket

// 3) Öffentliche Mapper-Funktion (so nutzt der Rest der App den Mapper)
export function mapTicket(dto: TicketDto): Ticket {
  return mapBySpec(dto, ticketSpec);
}
