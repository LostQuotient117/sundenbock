package de.nak.iaa.sundenbock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.PageDTO;
import de.nak.iaa.sundenbock.dto.mapper.ProjectMapper;
import de.nak.iaa.sundenbock.dto.projectDTO.CreateProjectDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.*;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.service.ProjectService;
import de.nak.iaa.sundenbock.service.security.CustomSecurityService;
import de.nak.iaa.sundenbock.service.security.JwtService;
import de.nak.iaa.sundenbock.service.user.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectMapper projectMapper;

    @MockitoBean
    private PageableFactory pageableFactory;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    private UserDTO testUserDTO;
    private ProjectDTO testProjectDTO;
    private Project testProject;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO(1L, "testuser", "Test", "User");
        testProjectDTO = new ProjectDTO(
                1L, "Test Project", "Description", "TPJ",
                Instant.now(), Instant.now(), testUserDTO, testUserDTO
        );

        testProject = new Project();
        testProject.setId(1L);
        testProject.setTitle("Test Project");
        testProject.setAbbreviation("TPJ");

        testPageable = PageRequest.of(0, 20);

        when(pageableFactory.createPageable(
                anyInt(),
                anyInt(),
                nullable(String.class), // War 'anyString()'
                anySet(),
                anyMap()
        )).thenReturn(testPageable);
    }

    @Test
    @DisplayName("GET /api/v1/projects should return 401 for anonymous user")
    @WithAnonymousUser
    void getProjects_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/1 should return 401 for anonymous user")
    @WithAnonymousUser
    void getProjectById_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/projects/create should return 401 for anonymous user")
    @WithAnonymousUser
    void createProject_shouldReturn401_forAnonymous() throws Exception {
        CreateProjectDTO createDTO = new CreateProjectDTO("Title", "Desc", "ABC");
        mockMvc.perform(post("/api/v1/projects/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/1/update should return 401 for anonymous user")
    @WithAnonymousUser
    void updateProject_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(put("/api/v1/projects/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProjectDTO)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("GET /api/v1/projects should return 403 for user without PROJECT_READ")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void getProjects_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/1 should return 403 for user without PROJECT_READ")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void getProjectById_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/projects/create should return 403 for user without PROJECT_CREATE")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void createProject_shouldReturn403_forUserWithoutPermission() throws Exception {
        CreateProjectDTO createDTO = new CreateProjectDTO("Title", "Desc", "ABC");
        mockMvc.perform(post("/api/v1/projects/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/1/update should return 403 for user without PROJECT_UPDATE")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void updateProject_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(put("/api/v1/projects/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProjectDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/projects should return paged list for user with PROJECT_READ")
    @WithMockUser(authorities = "PROJECT_READ")
    void getProjects_shouldReturnPagedList() throws Exception {
        Page<Project> projectPage = new PageImpl<>(List.of(testProject), testPageable, 1);
        PageDTO<ProjectDTO> pageDTO = PageDTO.of(List.of(testProjectDTO), 1, 0, 20);

        // Dieser Mock sollte jetzt greifen, da pageableFactory testPageable zurückgibt
        when(projectService.search(isNull(), eq(testPageable))).thenReturn(projectPage);
        when(projectMapper.toProjectDTOs(any())).thenReturn(pageDTO.items());

        mockMvc.perform(get("/api/v1/projects")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].abbreviation").value("TPJ"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/1 should return project for user with PROJECT_READ")
    @WithMockUser(authorities = "PROJECT_READ")
    void getProjectById_shouldReturnProject() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(testProjectDTO);

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.abbreviation").value("TPJ"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/create should return 200 OK and created project")
    @WithMockUser(authorities = "PROJECT_CREATE")
    void createProject_shouldReturnOk_andProject() throws Exception {
        CreateProjectDTO createDTO = new CreateProjectDTO("New Project", "Desc", "NEW");

        when(projectService.createProject(any(CreateProjectDTO.class))).thenReturn(testProjectDTO);

        mockMvc.perform(post("/api/v1/projects/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abbreviation").value("TPJ"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/1/update should return 200 OK and updated project")
    @WithMockUser(authorities = "PROJECT_UPDATE")
    void updateProject_shouldReturnOk_andUpdatedProject() throws Exception {
        when(projectService.updateProject(eq(1L), any(ProjectDTO.class))).thenReturn(testProjectDTO);

        mockMvc.perform(put("/api/v1/projects/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProjectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/projects/99 should return 404 if not found")
    @WithMockUser(authorities = "PROJECT_READ")
    void getProjectById_shouldReturn404_ifNotFound() throws Exception {
        when(projectService.getProjectById(99L)).thenThrow(new ResourceNotFoundException("Project not found"));

        mockMvc.perform(get("/api/v1/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/0 should return 400 for invalid path variable")
    @WithMockUser(authorities = "PROJECT_READ")
    void getProjectById_shouldReturn400_forInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/projects/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/create should return 400 for invalid DTO")
    @WithMockUser(authorities = "PROJECT_CREATE")
    void createProject_shouldReturn400_forInvalidDTO() throws Exception {
        CreateProjectDTO badDTO = new CreateProjectDTO("", "Desc", "INVALID");

        mockMvc.perform(post("/api/v1/projects/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be empty"))
                .andExpect(jsonPath("$.fieldErrors.abbreviation").value("Abbreviation must be exactly 3 characters long"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/1/update should return 400 for ID mismatch")
    @WithMockUser(authorities = "PROJECT_UPDATE")
    void updateProject_shouldReturn400_forIdMismatch() throws Exception {
        ProjectDTO mismatchDTO = new ProjectDTO(
                2L, "Mismatch Project", "Desc", "MIS",
                Instant.now(), Instant.now(), testUserDTO, testUserDTO
        );

        when(projectService.updateProject(eq(1L), any(ProjectDTO.class)))
                .thenThrow(new MismatchedIdException("Path variable 'id' = 1 does not match 'id' = 2 in request body"));


        mockMvc.perform(put("/api/v1/projects/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Path variable 'id' = 1 does not match 'id' = 2 in request body"));
    }

    @Test
    @WithMockUser(authorities = "PROJECT_DELETE")
    void deleteProject_shouldReturnOk_whenSuccessful() throws Exception {
        Long projectId = 1L;
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/v1/projects/{id}/delete", projectId))
                .andExpect(status().isOk());
        verify(projectService).deleteProject(projectId);
    }

    @Test
    @WithMockUser(authorities = "PROJECT_DELETE")
    void deleteProject_shouldReturnNotFound_whenProjectDoesNotExist() throws Exception {
        Long projectId = 99L;
        doThrow(new ResourceNotFoundException("Project not found with id " + projectId))
                .when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/v1/projects/{id}/delete", projectId))
                .andExpect(status().isNotFound());

        verify(projectService).deleteProject(projectId);
    }

    @Test
    @WithMockUser(authorities = "PROJECT_DELETE")
    void deleteProject_shouldReturnConflict_whenProjectHasOpenTickets() throws Exception {
        Long projectId = 1L;
        doThrow(new ProjectHasOpenTicketsException("Project cannot be deleted, it still has 2 open ticket(s)."))
                .when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/v1/projects/{id}/delete", projectId))
                .andExpect(status().isConflict());

        verify(projectService).deleteProject(projectId);
    }

    @Test
    @WithMockUser(authorities = "PROJECT_DELETE")
    void deleteProject_shouldReturnBadRequest_whenIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/{id}/delete", 0L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @WithMockUser(authorities = "PROJECT_READ")
    void deleteProject_shouldReturnForbidden_whenMissingAuthority() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/{id}/delete", 1L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(projectService);
    }
}