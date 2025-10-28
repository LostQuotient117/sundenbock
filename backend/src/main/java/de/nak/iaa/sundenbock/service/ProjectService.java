package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.ProjectDTO;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
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
                .orElseThrow(() -> new RuntimeException("Project not found")); //TODO: Exception
        return projectMapper.toProjectDTO(project);
    }
    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = projectMapper.toProject(projectDTO);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectDTO(savedProject);
    }
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
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
