package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.exception.InvalidStatusTransitionException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.exception.TicketAlreadyClosedException;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.UUID;

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
     * Searches tickets by a free-text query across multiple fields.
     * <p>
     * If {@code query} is provided (non-null and non-empty), a case-insensitive LIKE
     * filter is applied to the following attributes: ticket title, description, status,
     * responsible person's username, project's title, and creator's username. If the
     * {@code query} is blank, all tickets are returned paginated.
     * </p>
     *
     * @param query    optional free-text filter; when blank, no filtering is applied
     * @param pageable pagination and sorting information
     * @return a {@link Page} of {@link Ticket} entities matching the filter
     */
    @Transactional(readOnly = true)
    public Page<Ticket> search(String query, Pageable pageable) {
        Specification<Ticket> spec = null;

        if (StringUtils.hasText(query)) {
            String like = "%" + query.toLowerCase() + "%";
            spec = (root, cq, cb) -> {
                Join<Ticket, User> resp = root.join("responsiblePerson", JoinType.LEFT);
                Join<Ticket, User> creator = root.join("createdBy", JoinType.LEFT);
                Join<Ticket, Project> proj = root.join("project", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("status").as(String.class)), like),
                        cb.like(cb.lower(resp.get("username")), like),
                        cb.like(cb.lower(proj.get("title")), like),
                        cb.like(cb.lower(creator.get("username")), like),
                        cb.like(cb.lower(root.get("ticketKey")), like)
                );
            };
        }

        return ticketRepository.findAll(spec, pageable);
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

        Ticket savedTicket = saveWithTicketKey(ticket);
        return ticketMapper.toTicketDTO(savedTicket);
    }

    private  Ticket saveWithTicketKey(Ticket ticket) {
        ticket.setTicketKey(ticket.getProject().getAbbreviation() + "-" + UUID.randomUUID().toString().substring(0, 8));
        Ticket saved = ticketRepository.save(ticket);
        saved.setTicketKey(saved.getProject().getAbbreviation() + "-" + saved.getId());
        return ticketRepository.save(saved);
    }

    /**
     * Updates an existing ticket identified by its ID, applying new data from the provided DTO.
     * <p>
     * This method operates within a transaction and includes several critical business logic checks:
     * <ul>
     * <li><b>Status Validation:</b> It verifies that the ticket is not already 'CLOSED'.</li>
     * <li><b>Role-Based Transition:</b> It validates the requested status transition based on the
     * authenticated user's roles (ADMIN, DEVELOPER, or AUTHOR).
     * <ul>
     * <li>{@code ROLE_ADMIN} can perform any transition.</li>
     * <li>{@code ROLE_DEVELOPER} can only perform transitions defined in
     * {@code TicketStatus.getAllowedTransitionsForDeveloper()}.</li>
     * <li>The <b>Author</b> (original creator) can only perform transitions defined in
     * {@code TicketStatus.getAllowedTransitionsForAuthor()}.</li>
     * <li>Staying in the same status is always permitted.</li>
     * </ul>
     * </li>
     * <li><b>Entity Updates:</b> It updates the responsible person and/or the associated project
     * if they are provided in the DTO, fetching and validating their existence.</li>
     * <li><b>Data Mapping:</b> It uses a mapper to apply all other updates from the DTO to the
     * ticket entity before persistence.</li>
     * </ul>
     *
     * @param id        The unique identifier of the ticket to update.
     * @param ticketDTO A {@link TicketDTO} containing the new data for the ticket, including the
     * desired {@code nextStatus}.
     * @return The updated {@link TicketDTO} after the changes have been persisted.
     * @throws ResourceNotFoundException      if the ticket with the specified {@code id} is not found,
     * or if the responsible user or project specified in the
     * DTO cannot be found by their respective identifiers.
     * @throws TicketAlreadyClosedException   if an update is attempted on a ticket that is already
     * in the {@code CLOSED} status.
     * @throws InvalidStatusTransitionException if the transition from the ticket's current status
     * to the new status is not allowed for the
     * authenticated user's role.
     */
    @Transactional
    public TicketDTO updateTicket(Long id, TicketDTO ticketDTO) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));

        TicketStatus currentStatus = existingTicket.getStatus();
        TicketStatus nextStatus = ticketDTO.status();

        if (currentStatus == TicketStatus.CLOSED){
            throw new TicketAlreadyClosedException("Ticket with id " + existingTicket.getId() + " is already closed");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isDeveloper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"));
        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(existingTicket.getCreatedBy().getUsername()));

        boolean valid = isAdmin ||
                (isDeveloper && currentStatus.getAllowedTransitionsForDeveloper().contains(nextStatus)) ||
                (isAuthor && currentStatus.getAllowedTransitionsForAuthor().contains(nextStatus)) ||
                (currentStatus == nextStatus);

        if (!valid) {
            throw new InvalidStatusTransitionException(String.format("Transition from %s to %s not allowed", currentStatus, nextStatus));
        }

        if (ticketDTO.responsiblePerson() != null && ticketDTO.responsiblePerson().username() != null) {
            User responsible = userRepository.findByUsername(ticketDTO.responsiblePerson().username())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + ticketDTO.responsiblePerson().username()));
            existingTicket.setResponsiblePerson(responsible);
        }

        if (ticketDTO.project() != null && ticketDTO.project().id() != null) {
            Project project = projectRepository.findById(ticketDTO.project().id())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + ticketDTO.project().id()));
            existingTicket.setProject(project);
        }

        ticketMapper.updateTicketFromDTO(ticketDTO, existingTicket, userRepository);
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
