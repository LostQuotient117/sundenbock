package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
