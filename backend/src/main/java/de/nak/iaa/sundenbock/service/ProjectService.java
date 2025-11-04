package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toProjectDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
        return projectMapper.toProjectDTO(project);
    }
    @Transactional
    public ProjectDTO createProject(CreateProjectDTO createProjectDTO) {
        if (projectRepository.existsByTitle(createProjectDTO.title())) {
            throw new DuplicateResourceException("Project with title '" +  createProjectDTO.title() + "' already exists");
        }
        Project project = projectMapper.toProjectForCreate(createProjectDTO);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectDTO(savedProject);
    }
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
        existingProject.setDescription(projectDTO.description());
        existingProject.setTitle(projectDTO.title());

        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toProjectDTO(updatedProject);
    }
    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
