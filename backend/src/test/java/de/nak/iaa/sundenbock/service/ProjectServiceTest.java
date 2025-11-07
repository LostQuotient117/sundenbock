package de.nak.iaa.sundenbock.service;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.DuplicateResourceException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectDTO projectDTO;
    private CreateProjectDTO createProjectDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("creator");
        user.setFirstName("Test");
        user.setLastName("User");

        userDTO = new UserDTO(1L, "creator", "Test", "User");

        project = new Project();
        project.setId(1L);
        project.setTitle("Test Project");
        project.setDescription("Test Description");
        project.setAbbreviation("TPR");
        project.setCreatedBy(user);
        project.setLastModifiedBy(user);
        project.setCreatedDate(Instant.now());
        project.setLastModifiedDate(Instant.now());

        projectDTO = new ProjectDTO(
                1L,
                "Test Project",
                "Test Description",
                "TPR",
                project.getCreatedDate(),
                project.getLastModifiedDate(),
                userDTO,
                userDTO
        );

        createProjectDTO = new CreateProjectDTO(
                "New Project",
                "New Description",
                "NPR"
        );
    }

    @Test
    void getProjectById_returnsDTO_whenFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDTO(project)).thenReturn(projectDTO);

        ProjectDTO result = projectService.getProjectById(1L);

        assertEquals(projectDTO, result);
        verify(projectRepository).findById(1L);
        verify(projectMapper).toProjectDTO(project);
    }

    @Test
    void getProjectById_throwsException_whenNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.getProjectById(1L));

        verify(projectRepository).findById(1L);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void createProject_returnsMappedDTO_whenSuccessful() {
        Project newProject = new Project();
        newProject.setTitle(createProjectDTO.title());
        newProject.setDescription(createProjectDTO.description());
        newProject.setAbbreviation(createProjectDTO.abbreviation());

        Project savedProject = new Project();
        savedProject.setId(2L);
        savedProject.setTitle(createProjectDTO.title());

        ProjectDTO newProjectDTO = new ProjectDTO(2L, createProjectDTO.title(), createProjectDTO.description(), createProjectDTO.abbreviation(), Instant.now(), Instant.now(), userDTO, userDTO);

        when(projectRepository.existsByTitle(createProjectDTO.title())).thenReturn(false);
        when(projectMapper.toProjectForCreate(createProjectDTO)).thenReturn(newProject);
        when(projectRepository.save(newProject)).thenReturn(savedProject);
        when(projectMapper.toProjectDTO(savedProject)).thenReturn(newProjectDTO);

        ProjectDTO result = projectService.createProject(createProjectDTO);

        assertEquals(newProjectDTO, result);
        verify(projectRepository).existsByTitle(createProjectDTO.title());
        verify(projectMapper).toProjectForCreate(createProjectDTO);
        verify(projectRepository).save(newProject);
        verify(projectMapper).toProjectDTO(savedProject);
    }

    @Test
    void createProject_throwsException_whenTitleExists() {
        when(projectRepository.existsByTitle(createProjectDTO.title())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> projectService.createProject(createProjectDTO));

        verify(projectRepository).existsByTitle(createProjectDTO.title());
        verifyNoMoreInteractions(projectRepository);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void updateProject_returnsDTO_whenSuccessful() {
        ProjectDTO updatedDto = new ProjectDTO(
                1L,
                "Updated Title",
                "Updated Description",
                "TPR",
                projectDTO.createdDate(),
                Instant.now(),
                projectDTO.createdBy(),
                projectDTO.lastModifiedBy()
        );

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(updatedDto);

        ProjectDTO result = projectService.updateProject(1L, updatedDto);

        assertEquals(updatedDto.title(), result.title());
        assertEquals(updatedDto.description(), result.description());
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(project);
        assertEquals("Updated Title", project.getTitle());
        assertEquals("Updated Description", project.getDescription());
    }

    @Test
    void updateProject_throwsException_whenNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.updateProject(1L, projectDTO));

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void deleteProject_callsRepositoryDelete() {
        projectService.deleteProject(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void search_returnsPage_withQuery() {
        Page<Project> page = new PageImpl<>(List.of(project));
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findAll(ArgumentMatchers.<Specification<Project>>any(), any(Pageable.class))).thenReturn(page);

        Page<Project> result = projectService.search("test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(projectRepository).findAll(ArgumentMatchers.<Specification<Project>>any(), eq(pageable));
    }

    @Test
    void search_returnsPage_withoutQuery() {
        Page<Project> page = new PageImpl<>(List.of(project));
        Pageable pageable = PageRequest.of(0, 10);

        when(projectRepository.findAll(ArgumentMatchers.<Specification<Project>>isNull(), any(Pageable.class))).thenReturn(page);

        Page<Project> result = projectService.search(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(projectRepository).findAll(ArgumentMatchers.<Specification<Project>>isNull(), eq(pageable));

        Page<Project> resultBlank = projectService.search("", pageable);
        assertEquals(1, resultBlank.getTotalElements());
        verify(projectRepository, times(2)).findAll(ArgumentMatchers.<Specification<Project>>isNull(), eq(pageable));
    }
}
