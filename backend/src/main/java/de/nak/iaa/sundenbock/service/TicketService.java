package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.TicketDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getTickets() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toTicketDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found")); //TODO: Exception
        return ticketMapper.toTicketDTO(ticket);
    }

    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Ticket ticket = ticketMapper.toTicket(ticketDTO);
        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toTicketDTO(savedTicket);
    }

    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        existingTicket.setTitle(ticketDTO.title());
        existingTicket.setDescription(ticketDTO.description());
        existingTicket.setStatus(ticketDTO.status());
        existingTicket.setResponsiblePerson(ticketDTO.responsiblePerson());
        existingTicket.setProject(ticketDTO.project());
        //TODO: automatic change of lastupdatet through @preupdate?

        Ticket updatedTicket = ticketRepository.save(existingTicket);
        return ticketMapper.toTicketDTO(updatedTicket);
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
