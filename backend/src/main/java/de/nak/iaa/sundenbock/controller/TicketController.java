package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.TicketDTO;
import de.nak.iaa.sundenbock.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Endpoint: Get all Tickets (GET /api/tickets)
    @GetMapping
    public List<TicketDTO> getTickets() {
        return ticketService.getTickets();
    }

    // Endpoint: Get Ticket by ID (GET /api/tickets/{id})
    @GetMapping("/{id}")
    public TicketDTO getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    // Endpoint: Create a new Ticket (POST /api/tickets)
    @PostMapping
    public TicketDTO createTicket(@RequestBody TicketDTO ticketDTO) {
        return ticketService.createTicket(ticketDTO);
    }

    // Endpoint: Update a Ticket (PUT /api/tickets/{id})
    @PutMapping("/{id}")
    public TicketDTO updateTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDTO) {
        return ticketService.updateTicket(id, ticketDTO);
    }

    // Endpoint: Delete a Ticket (DELETE /api/tickets/{id})
    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
    }

    //TODO: ticket by filters (status, author, developer, prio etc etc)
}
