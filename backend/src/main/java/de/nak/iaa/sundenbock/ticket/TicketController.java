package de.nak.iaa.sundenbock.ticket;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    //Endpoint: Get all Tickets (GET /api/tickets)
    @GetMapping
    public List<Ticket> getTickets() {
        return ticketRepository.findAll();
    }

    //Endpoint: Get Ticket by id (GET /api/tickets/{id})
    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id){
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket Not Found"));
    }

    //Endpoint: Create a new Ticket (POST /api/tickets)
    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket){
        return ticketRepository.save(ticket);
    }

    // Endpoint: update a Ticket (PUT /api/tickets/{id})
    @PutMapping("/{id}")
    public Ticket updateTicket(@PathVariable Long id, @RequestBody Ticket ticketDetails) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket nicht gefunden"));

        ticket.setTitle(ticketDetails.getTitle());
        ticket.setDescription(ticketDetails.getDescription());
        ticket.setStatus(ticketDetails.getStatus());
        ticket.setResponsiblePerson(ticketDetails.getResponsiblePerson());
        ticket.setProject(ticketDetails.getProject());

        return ticketRepository.save(ticket);
    }

    // Endpoint: delete a Ticket (DELETE /api/tickets/{id})
    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) {
        ticketRepository.deleteById(id);
    }

    //TODO: ticket by filters (status, author, developer, prio etc etc)
}
