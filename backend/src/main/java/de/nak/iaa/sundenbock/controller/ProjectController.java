package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.annotation.NavItem;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.PageDTO;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * REST controller exposing CRUD operations for projects.
 * <p>
 * Base path: {@code api/v1/projects}
 * </p>
 * <p>
 * All request payloads are validated using Jakarta Bean Validation. Path variables annotated with
 * {@link Min} ensure positive identifiers.
 * </p>
 */
@NavItem(label = "Projects", path = "/projects", icon = "project")
@RestController
@RequestMapping("api/v1/projects")
@Validated
public class ProjectController {
    private final ProjectService projectService;

    private static final Map<String,String> SORT_ALIAS = Map.of("createdOn","createdDate",
                                                                    "creator", "createdBy.username");
    private static final Set<String> SORT_WHITELIST = Set.of("createdDate","lastModifiedDate","title", "createdBy.username");
    private final PageableFactory pageableFactory;
    private final ProjectMapper projectMapper;

    /**
     * Creates a new instance of the controller.
     *
     * @param projectService service handling the project business logic
     */
    public ProjectController(ProjectService projectService, PageableFactory pageableFactory, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.pageableFactory = pageableFactory;
        this.projectMapper = projectMapper;
    }
    
    /**
     * Retrieves all projects.
     *
     * @return list of all existing projects as {@link ProjectDTO}
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_READ')")
    public PageDTO<ProjectDTO> getProjects(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = pageableFactory.createPageable(page, pageSize, sort, SORT_WHITELIST, SORT_ALIAS);
        Page<Project> p = projectService.search(search, pageable);
        return PageDTO.of(projectMapper.toProjectDTOs(p.getContent()), p.getTotalElements(), page, pageSize);
    }
    
    /**
     * Retrieves a single project by its identifier.
     *
     * @param id the project id, must be greater than or equal to 1
     * @return the project with the given id as {@link ProjectDTO}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_READ')")
    public ProjectDTO getProjectById(@PathVariable @Min(1) Long id) {
        return projectService.getProjectById(id);
    }

    /**
     * Creates a new project.
     *
     * @param createProjectDTO payload containing the data required to create a project; validated
     * @return the created project as {@link ProjectDTO}
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    public ProjectDTO createProject(@Valid @RequestBody CreateProjectDTO createProjectDTO) {
        return projectService.createProject(createProjectDTO);
    }
    
    /**
     * Updates an existing project.
     *
     * @param id          the id of the project to update, must be greater than or equal to 1
     * @param projectDTO  payload containing the updated project data; validated
     * @return the updated project as {@link ProjectDTO}
     */
    @PutMapping("/{id}/update")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    public ProjectDTO updateProject(@PathVariable @Min(1) Long id ,@Valid @RequestBody ProjectDTO projectDTO) {
        if (Objects.equals(projectDTO.id(), id)) {
            return projectService.updateProject(id, projectDTO);
        } else {
            throw new MismatchedIdException("Path variable 'id' = " + id + " does not match 'id' = " + projectDTO.id() +" in request body");
        }
    }

    /**
     * Deletes a project by its ID.
     * <p>
     * A project can only be deleted if it has no open tickets. Tickets with status
     * {@code CLOSED} or {@code REJECTED} are not considered open.
     * </p>
     *
     * @param id the ID of the project to delete
     */
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProject(@PathVariable @Min(1) Long id) {
        projectService.deleteProject(id);
    }
}
