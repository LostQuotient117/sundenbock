import { HydratedTicket } from '../../shared/models/types';
import { Page } from '../../shared/models/paging';

export const MOCK_TICKETS: Page<HydratedTicket> = {
  items: [
    {
      id: 1,
      title: 'Login-Fehler auf Prod',
      status: 'IN_PROGRESS',
      createdOn: new Date('2025-11-01T10:12:00'),
      lastChange: new Date('2025-11-02T08:15:00')
    },
    {
      id: 2,
      title: 'Fehlerhafte Summenanzeige',
      status: 'CREATED',
      createdOn: new Date('2025-10-28T09:30:00'),
      lastChange: new Date('2025-10-29T12:10:00')
    }
  ],
  total: 2,
  page: 0,
  pageSize: 20
};