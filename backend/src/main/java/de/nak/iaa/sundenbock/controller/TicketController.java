package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * REST controller exposing CRUD operations for tickets.
 * <p>
 * Base path: {@code /api/v1/tickets}
 * </p>
 * <p>
 * All request payloads are validated using Jakarta Bean Validation. Path variables annotated with
 * {@link Min} ensure positive identifiers.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Validated
public class TicketController {
    private final TicketService ticketService;

    /**
     * Creates a new instance of the controller.
     *
     * @param ticketService service handling the ticket business logic
     */
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Retrieves all tickets.
     *
     * @return list of all existing tickets as {@link TicketDTO}
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TICKET_READ_ALL')")
    public List<TicketDTO> getTickets() {
        return ticketService.getTickets();
    }

    /**
     * Retrieves a single ticket by its identifier.
     *
     * @param id the ticket id, must be greater than or equal to 1
     * @return the ticket with the given id as {@link TicketDTO}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKET_READ_ALL') or @customSecurityService.canAccessTicket(#id, authentication)")
    public TicketDTO getTicketById(@PathVariable @Min(1) Long id) {
        return ticketService.getTicketById(id);
    }

    /**
     * Creates a new ticket.
     *
     * @param createTicketDTO payload containing the data required to create a ticket; validated
     * @return the created ticket as {@link TicketDTO}
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TICKET_CREATE')")
    public TicketDTO createTicket(@Valid @RequestBody CreateTicketDTO createTicketDTO) {
        return ticketService.createTicket(createTicketDTO);
    }

    /**
     * Updates an existing ticket.
     *
     * @param id        the id of the ticket to update, must be greater than or equal to 1
     * @param ticketDTO payload containing the updated ticket data; validated
     * @return the updated ticket as {@link TicketDTO}
     */
    @PutMapping("/{id}/update")
    @PreAuthorize("@customSecurityService.canUpdateTicket(#id, authentication)")
    public TicketDTO updateTicket(@PathVariable @Min(1) Long id, @Valid @RequestBody TicketDTO ticketDTO) {
        if (Objects.equals(ticketDTO.id(), id)) {
            return ticketService.updateTicket(id, ticketDTO);
        } else {
            throw new MismatchedIdException("Path variable 'id' = " + id + " does not match 'id' = " + ticketDTO.id() + "in request body");
        }
    }

    /**
     * Deletes a ticket by its identifier.
     *
     * @param id the id of the ticket to delete, must be greater than or equal to 1
     */
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('TICKET_DELETE')")
    public void deleteTicket(@PathVariable @Min(1) Long id) {
        ticketService.deleteTicket(id);
    }
}
