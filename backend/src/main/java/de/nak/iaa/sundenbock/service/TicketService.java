package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.TicketDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import jakarta.transaction.Transactional;
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

    private TicketDTO convertToDTO(Ticket ticket) {
        return ticketMapper.toTicketDTO(ticket);
    }

    private Ticket convertToEntity(TicketDTO ticketDTO) {
        return ticketMapper.toTicket(ticketDTO);
    }

    @Transactional
    public List<TicketDTO> getTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found")); //TODO: Exception
        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Ticket ticket = convertToEntity(ticketDTO);
        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
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
        return convertToDTO(updatedTicket);
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
