package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@Validated
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketDTO> getTickets() {
        return ticketService.getTickets();
    }

    @GetMapping("/{id}")
    public TicketDTO getTicketById(@PathVariable @Min(1) Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping("/create")
    public TicketDTO createTicket(@Valid @RequestBody CreateTicketDTO createTicketDTO) {
        return ticketService.createTicket(createTicketDTO);
    }

    @PutMapping("/{id}/update")
    public TicketDTO updateTicket(@PathVariable @Min(1) Long id, @Valid @RequestBody TicketDTO ticketDTO) {
        return ticketService.updateTicket(id, ticketDTO);
    }

    @DeleteMapping("/{id}/delete")
    public void deleteTicket(@PathVariable @Min(1) Long id) {
        ticketService.deleteTicket(id);
    }
}
