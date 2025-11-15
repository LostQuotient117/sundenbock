package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ProjectHasOpenTicketsException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Service class for managing {@link Project} entities.
 * <p>
 * Provides CRUD operations and mapping between entities and DTOs via {@link ProjectMapper}.
 * Ensures uniqueness constraints (e.g., title) and translates repository results into {@link ProjectDTO}s.
 * </p>
 */
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    /**
     * Creates a new {@code ProjectService}.
     *
     * @param projectRepository repository for projects
     * @param projectMapper     mapper for project and DTO conversions
     */
    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    /**
     * Searches projects by a free-text query across multiple fields.
     * <p>
     * If {@code query} is non-blank, applies a case-insensitive LIKE filter on title,
     * description, and creator username. Otherwise returns all projects paginated.
     * </p>
     *
     * @param query    optional free-text filter; when blank, no filtering is applied
     * @param pageable pagination and sorting information
     * @return a {@link Page} of {@link Project} entities matching the filter
     */
    @Transactional(readOnly = true)
    public Page<Project> search(String query, Pageable pageable) {
        Specification<Project> spec = null;

        if (StringUtils.hasText(query)) {
            String like = "%" + query.toLowerCase() + "%";
            spec = (root, cq, cb) -> {
                Join<Project, User> creator = root.join("createdBy", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(creator.get("username")), like),
                        cb.like(cb.lower(root.get("abbreviation")), like)
                );
            };
        }

        return projectRepository.findAll(spec, pageable);
    }

    /**
     * Returns a single project by its ID.
     *
     * @param id the ID of the requested project
     * @return the found project as {@link ProjectDTO}
     * @throws ResourceNotFoundException if no project exists with the given ID
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
        return projectMapper.toProjectDTO(project);
    }

    /**
     * Creates a new project.
     * <p>
     * Validates that the title is unique before persisting.
     * </p>
     *
     * @param createProjectDTO data for creating the project
     * @return the created project as {@link ProjectDTO}
     * @throws DuplicateResourceException if a project with the same title already exists
     */
    @Transactional
    public ProjectDTO createProject(CreateProjectDTO createProjectDTO) {
        if (projectRepository.existsByTitle(createProjectDTO.title())) {
            throw new DuplicateResourceException("Project with title '" +  createProjectDTO.title() + "' already exists");
        }
        String abbreviation = createProjectDTO.abbreviation().toUpperCase();
        if (projectRepository.existsByAbbreviation(abbreviation)) {
            throw new DuplicateResourceException("Project with abbreviation '" + abbreviation + "' already exists");
        }
        Project project = projectMapper.toProjectForCreate(createProjectDTO);
        project.setAbbreviation(abbreviation);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectDTO(savedProject);
    }

    /**
     * Updates an existing project with the data from the provided {@link ProjectDTO}.
     * Only the mutable fields (title, description) are updated.
     *
     * @param id         the ID of the project to update
     * @param projectDTO the new values for the project
     * @return the updated project as {@link ProjectDTO}
     * @throws ResourceNotFoundException if no project exists with the given ID
     */
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        projectRepository.findByTitle(projectDTO.title()).ifPresent(p -> {
            if (!Objects.equals(p.getId(), existingProject.getId())) {
                throw new DuplicateResourceException("Project with title '" + projectDTO.title() + "' already exists");
            }
        });

        String newAbbreviation = projectDTO.abbreviation().toUpperCase();
        projectRepository.findByAbbreviation(newAbbreviation).ifPresent(p -> {
            if (!Objects.equals(p.getId(), existingProject.getId())) {
                throw new DuplicateResourceException("Project with abbreviation '" + newAbbreviation + "' already exists");
            }
        });

        existingProject.setTitle(projectDTO.title());
        existingProject.setDescription(projectDTO.description());
        existingProject.setAbbreviation(newAbbreviation);

        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toProjectDTO(updatedProject);
    }

    /**
     * Deletes a project by its ID.
     * <p>
     * A project can only be deleted if it has no open tickets. Tickets with status
     * {@code CLOSED} or {@code REJECTED} are not considered open.
     * </p>
     *
     * @param id the ID of the project to delete
     * @throws ResourceNotFoundException      if no project exists with the given ID
     * @throws ProjectHasOpenTicketsException if the project still has open tickets
     */
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        long openTickets = project.getTickets().stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.CLOSED && ticket.getStatus() != TicketStatus.REJECTED)
                .count();

        if (openTickets > 0) {
            throw new ProjectHasOpenTicketsException("Project cannot be deleted, it still has " + openTickets + " open ticket(s).");
        }

        projectRepository.delete(project);
    }
}
