package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
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

/**
 * Service class for managing {@link Ticket} entities.
 * <p>
 * Encapsulates business logic for CRUD operations on tickets and handles the
 * association to {@link User responsible users} as well as to {@link Project projects}.
 * Entities are also converted to DTOs via the {@link TicketMapper}.
 * </p>
 */
@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     * Creates a new {@code TicketService}.
     *
     * @param ticketRepository repository for tickets
     * @param ticketMapper     mapper for ticket and DTO conversions
     * @param userRepository   repository for users (to resolve the responsible user)
     * @param projectRepository repository for projects (to resolve the project)
     */
    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper, UserRepository userRepository, ProjectRepository projectRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Returns all existing tickets as a list of {@link TicketDTO}.
     *
     * @return list of all tickets in DTO representation
     */
    @Transactional(readOnly = true)
    public List<TicketDTO> getTickets() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toTicketDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single ticket by its ID.
     *
     * @param id the ID of the requested ticket
     * @return the found ticket as {@link TicketDTO}
     * @throws ResourceNotFoundException if no ticket exists with the given ID
     */
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));
        return ticketMapper.toTicketDTO(ticket);
    }

    /**
     * Creates a new ticket.
     * <p>
     * The responsible user is resolved by username and the associated project by project ID
     * from the respective repositories and set on the ticket.
     * </p>
     *
     * @param ticketDTO data for creating the ticket
     * @return the created ticket as {@link TicketDTO}
     * @throws ResourceNotFoundException if the responsible user or the project cannot be found
     */
    @Transactional
    public TicketDTO createTicket(CreateTicketDTO ticketDTO) {
        Ticket ticket = ticketMapper.toTicketFromCreate(ticketDTO);

        User responsible = userRepository.findByUsername(ticketDTO.responsiblePersonUserName())
                .orElseThrow(() -> new ResourceNotFoundException("Responsible person not found with username " + ticketDTO.responsiblePersonUserName()));
        ticket.setResponsiblePerson(responsible);

        Project project = projectRepository.findById(ticketDTO.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + ticketDTO.projectId()));
        ticket.setProject(project);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toTicketDTO(savedTicket);
    }

    /**
     * Updates an existing ticket with the data from the provided {@link TicketDTO}.
     *
     * @param id        the ID of the ticket to update
     * @param ticketDTO the new values for the ticket
     * @return the updated ticket as {@link TicketDTO}
     * @throws ResourceNotFoundException if no ticket exists with the given ID
     */
    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));

        ticketMapper.updateTicketFromDTO(ticketDTO, existingTicket);
        return ticketMapper.toTicketDTO(existingTicket);
    }

    /**
     * Deletes a ticket by its ID.
     *
     * @param id the ID of the ticket to delete
     * @throws ResourceNotFoundException if no ticket exists with the given ID
     */
    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket not found with id " + id);
        }

        ticketRepository.deleteById(id);
    }
}
