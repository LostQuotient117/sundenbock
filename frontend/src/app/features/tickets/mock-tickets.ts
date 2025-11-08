import { HydratedTicket } from '../../shared/models/types';
import { Page } from '../../shared/models/paging';

export const MOCK_TICKETS: Page<HydratedTicket> = {
  items: [
    {
      id: 1,
      ticketKey: 'TCK-001',
      title: 'Login-Fehler auf Prod',
      status: 'IN_PROGRESS',
      createdOn: new Date('2025-11-01T10:12:00'),
      lastChange: new Date('2025-11-02T08:15:00')
    },
    {
      id: 2,
      ticketKey: 'TCK-002',
      title: 'Fehlerhafte Summenanzeige',
      status: 'CREATED',
      createdOn: new Date('2025-10-28T09:30:00'),
      lastChange: new Date('2025-10-29T12:10:00')
    },
    {
      id: 3,
      ticketKey: 'TCK-003',
      title: 'Falsche Fehlermeldung bei Passwort-Reset',
      status: 'REOPENED',
      createdOn: new Date('2025-10-15T14:45:00'),
      lastChange: new Date('2025-10-20T09:00:00')
    },
    {
      id: 4,
      ticketKey: 'TCK-004',
      title: 'Layout-Anpassung für Dark Mode',
      status: 'RESOLVED',
      createdOn: new Date('2025-10-10T11:22:00'),
      lastChange: new Date('2025-10-13T16:00:00')
    },
    {
      id: 5,
      ticketKey: 'TCK-005',
      title: 'Button "Abbrechen" reagiert nicht',
      status: 'REJECTED',
      createdOn: new Date('2025-09-30T08:00:00'),
      lastChange: new Date('2025-10-01T10:45:00')
    },
    {
      id: 6,
      ticketKey: 'TCK-006',
      title: 'API-Dokumentation aktualisieren',
      status: 'CLOSED',
      createdOn: new Date('2025-09-20T13:30:00'),
      lastChange: new Date('2025-09-25T17:20:00')
    }
  ],
  total: 2,
  page: 0,
  pageSize: 20
};