package de.nak.iaa.sundenbock.repository;

import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for {@link Ticket} entities.
 * <p>
 * Extends {@link JpaRepository} for CRUD operations and
 * {@link JpaSpecificationExecutor} to support dynamic, criteria-based filtering.
 * </p>
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    /**
     * Counts how many tickets list this user as the responsible person.
     * @param user the user
     * @return the number of tickets
     */
    long countByResponsiblePerson(User user);

    /**
     * Counts how many tickets list this user as the creator.
     * @param user the user
     * @return the number of tickets
     */
    long countByCreatedBy(User user);

}
