package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.PageDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
@NavItem(label = "Tickets", path = "/tickets", icon = "ticket")
@RestController
@RequestMapping("/api/v1/tickets")
@Validated
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final PageableFactory pageableFactory;

    private static final Map<String,String> SORT_ALIAS = Map.of("createdOn","createdDate",
                                                                    "creator", "createdBy.username");
    private static final Set<String> SORT_WHITELIST = Set.of("createdDate","lastModifiedDate","title","status", "createdBy.username");

    /**
     * Creates a new instance of the controller.
     *
     * @param ticketService service handling the ticket business logic
     */
    public TicketController(TicketService ticketService, TicketMapper ticketMapper, PageableFactory pageableFactory) {
        this.ticketService = ticketService;
        this.ticketMapper = ticketMapper;
        this.pageableFactory = pageableFactory;
    }

    /**
     * Retrieves a paginated and sortable list of all tickets, optionally filtered
     * by a search term.
     * <p>
     * This endpoint supports pagination via {@code page} and {@code pageSize} parameters
     * and sorting via the {@code sort} parameter. The search term is applied
     * across multiple fields as defined in the {@code TicketService}.
     * <p>
     * Access to this endpoint is restricted to users who possess the
     * 'TICKET_READ_ALL' authority.
     *
     * @param search   (Optional) A search term used to filter tickets. The search
     * is typically applied to fields like title, description, etc.
     * If null or empty, all tickets are considered.
     * @param page     (Optional) The page number to retrieve, 0-indexed.
     * Defaults to 0.
     * @param pageSize (Optional) The number of tickets to retrieve per page.
     * Defaults to 20.
     * @param sort     (Optional) A string defining the sorting criteria, e.g.,
     * "title,asc" or "status,desc". The fields must be
     * present in the {@code SORT_WHITELIST}.
     * @return A {@link PageDTO} containing the list of {@link TicketDTO}s for the
     * requested page, along with total element count and pagination details.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TICKET_READ_ALL')")
    public PageDTO<TicketDTO> getTickets(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = pageableFactory.createPageable(page, pageSize, sort, SORT_WHITELIST, SORT_ALIAS);
        Page<Ticket> p = ticketService.search(search, pageable);
        return PageDTO.of(ticketMapper.toTicketDTOs(p.getContent()), p.getTotalElements(), page, pageSize);
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
