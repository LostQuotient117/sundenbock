package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/projects")
@Validated
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    @GetMapping
    public List<ProjectDTO> getAllProjects() {
        return projectService.getProjects();
    }
    
    @GetMapping("/{id}")
    public ProjectDTO getProjectById(@PathVariable @Min(1) Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping("/create")
    public ProjectDTO createProject(@Valid @RequestBody CreateProjectDTO createProjectDTO) {
        return projectService.createProject(createProjectDTO);
    }
    
    @PutMapping("/{id}/update")
    public ProjectDTO updateProject(@PathVariable @Min(1) Long id ,@Valid @RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(id, projectDTO);
    }

    @DeleteMapping("/{id}/delete")
    public void deleteProject(@PathVariable @Min(1) Long id) {
        projectService.deleteProject(id);
    }
}
