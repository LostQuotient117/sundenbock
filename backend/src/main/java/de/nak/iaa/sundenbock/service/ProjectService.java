package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.project.Project;
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
                        cb.like(cb.lower(creator.get("username")), like)
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
        Project project = projectMapper.toProjectForCreate(createProjectDTO);
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
        existingProject.setDescription(projectDTO.description());
        existingProject.setTitle(projectDTO.title());

        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toProjectDTO(updatedProject);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id the ID of the project to delete
     */
    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
