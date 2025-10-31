package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper, UserRepository userRepository, ProjectRepository projectRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
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
    public TicketDTO createTicket(CreateTicketDTO ticketDTO) {
        Ticket ticket = ticketMapper.toTicketFromCreate(ticketDTO);

        //User
        User responsible = userRepository.findByUsername(ticketDTO.responsiblePersonUserName())
                .orElseThrow(() -> new RuntimeException("User not found")); //TODO: Exception
        ticket.setResponsiblePerson(responsible);

        //Project
        Project project = projectRepository.findById(ticketDTO.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found")); //TODO: Exception
        ticket.setProject(project);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toTicketDTO(savedTicket);
    }

    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found")); //TODO: Exception

        ticketMapper.updateTicketFromDTO(ticketDTO, existingTicket);
        return ticketMapper.toTicketDTO(existingTicket);
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
