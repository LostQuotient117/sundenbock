package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RestController
@RequestMapping("api/v1/projects")
@Validated
public class ProjectController {
    private final ProjectService projectService;

    /**
     * Creates a new instance of the controller.
     *
     * @param projectService service handling the project business logic
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    /**
     * Retrieves all projects.
     *
     * @return list of all existing projects as {@link ProjectDTO}
     */
    @GetMapping
    public List<ProjectDTO> getAllProjects() {
        return projectService.getProjects();
    }
    
    /**
     * Retrieves a single project by its identifier.
     *
     * @param id the project id, must be greater than or equal to 1
     * @return the project with the given id as {@link ProjectDTO}
     */
    @GetMapping("/{id}")
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
    public ProjectDTO updateProject(@PathVariable @Min(1) Long id ,@Valid @RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(id, projectDTO);
    }

    /**
     * Deletes a project by its identifier.
     *
     * @param id the id of the project to delete, must be greater than or equal to 1
     */
    @DeleteMapping("/{id}/delete")
    public void deleteProject(@PathVariable @Min(1) Long id) {
        projectService.deleteProject(id);
    }
}
