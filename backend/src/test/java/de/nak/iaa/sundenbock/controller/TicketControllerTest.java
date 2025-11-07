package de.nak.iaa.sundenbock.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.frontendArchitectureDTO.PageDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.service.TicketService;
import de.nak.iaa.sundenbock.service.security.CustomSecurityService;
import de.nak.iaa.sundenbock.service.security.JwtService;
import de.nak.iaa.sundenbock.service.user.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private TicketMapper ticketMapper;

    @MockBean
    private PageableFactory pageableFactory;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomSecurityService customSecurityService;

    private TicketDTO testTicketDTO;
    private UserDTO testUserDTO;
    private ProjectDTO testProjectDTO;
    private Pageable testPageable;
    private Ticket testTicket; // Nötig für Mocks

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO(1L, "testuser", "Test", "User");
        testProjectDTO = new ProjectDTO(1L, "Test Project", "Desc", "TPJ", Instant.now(), Instant.now(), testUserDTO, testUserDTO);
        testTicketDTO = new TicketDTO(
                1L, "TPJ-1", "Test Ticket", "A description",
                TicketStatus.CREATED, testUserDTO, testProjectDTO,
                Instant.now(), Instant.now(), testUserDTO, testUserDTO
        );

        // Mock-Objekt für die Rückgabe des TicketService
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setTicketKey("TPJ-1");

        // Standard-Pageable mocken
        testPageable = PageRequest.of(0, 20);
        when(pageableFactory.createPageable(eq(0), eq(20), any(), any(), any()))
                .thenReturn(testPageable);
    }

    // --- Security Tests (Anonymous) ---

    @Test
    @DisplayName("GET /api/v1/tickets should return 401 for anonymous user")
    @WithAnonymousUser
    void getTickets_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/1 should return 401 for anonymous user")
    @WithAnonymousUser
    void getTicketById_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/tickets/create should return 401 for anonymous user")
    @WithAnonymousUser
    void createTicket_shouldReturn401_forAnonymous() throws Exception {
        CreateTicketDTO createDTO = new CreateTicketDTO("Title", "Desc", TicketStatus.CREATED, "user", 1L);
        mockMvc.perform(post("/api/v1/tickets/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
    }

    // --- Security Tests (Forbidden) ---

    @Test
    @DisplayName("GET /api/v1/tickets should return 403 for user without TICKET_READ_ALL")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void getTickets_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/1 should return 403 if not owner and no TICKET_READ_ALL")
    @WithMockUser(username = "otheruser", authorities = {"ROLE_USER"})
    void getTicketById_shouldReturn403_ifNotOwner() throws Exception {
        // Explizit mocken, dass der CustomSecurityService den Zugriff verweigert
        when(customSecurityService.canAccessTicket(eq(1L), any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/tickets/create should return 403 for user without TICKET_CREATE")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void createTicket_shouldReturn403_forUserWithoutPermission() throws Exception {
        CreateTicketDTO createDTO = new CreateTicketDTO("Title", "Desc", TicketStatus.CREATED, "user", 1L);
        mockMvc.perform(post("/api/v1/tickets/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/tickets/1/delete should return 403 for user without TICKET_DELETE")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void deleteTicket_shouldReturn403_forUserWithoutPermission() throws Exception {
        mockMvc.perform(delete("/api/v1/tickets/1/delete")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/tickets/1/update should return 403 if user cannot update")
    @WithMockUser(username = "otheruser", authorities = {"ROLE_USER"})
    void updateTicket_shouldReturn403_ifNotAllowed() throws Exception {
        when(customSecurityService.canUpdateTicket(eq(1L), any())).thenReturn(false);

        mockMvc.perform(put("/api/v1/tickets/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTicketDTO)))
                .andExpect(status().isForbidden());
    }

    // --- GET Endpoints (Authorized) ---

    @Test
    @DisplayName("GET /api/v1/tickets should return paged list for admin")
    @WithMockUser(authorities = "TICKET_READ_ALL")
    void getTickets_shouldReturnPagedList() throws Exception {
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket), testPageable, 1);
        PageDTO<TicketDTO> pageDTO = PageDTO.of(List.of(testTicketDTO), 1, 0, 20);

        when(ticketService.search(any(), any(Pageable.class))).thenReturn(ticketPage);
        when(ticketMapper.toTicketDTOs(any())).thenReturn(pageDTO.items());

        mockMvc.perform(get("/api/v1/tickets")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].ticketKey").value("TPJ-1"));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/1 should return ticket for user with TICKET_READ_ALL")
    @WithMockUser(authorities = "TICKET_READ_ALL")
    void getTicketById_shouldReturnTicket_forAdmin() throws Exception {
        when(ticketService.getTicketById(1L)).thenReturn(testTicketDTO);

        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ticketKey").value("TPJ-1"));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/1 should return ticket for owner via @customSecurityService")
    @WithMockUser(username = "testuser")
    void getTicketById_shouldReturnTicket_forOwner() throws Exception {
        when(customSecurityService.canAccessTicket(eq(1L), any())).thenReturn(true);
        when(ticketService.getTicketById(1L)).thenReturn(testTicketDTO);

        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // --- POST/PUT/DELETE Endpoints (Authorized) ---

    @Test
    @DisplayName("POST /api/v1/tickets/create should return 200 OK and created ticket")
    @WithMockUser(authorities = "TICKET_CREATE")
    void createTicket_shouldReturnOk_andTicket() throws Exception {
        CreateTicketDTO createDTO = new CreateTicketDTO("New Ticket", "Desc", TicketStatus.CREATED, "testuser", 1L);

        // Mocken, dass der Service das DTO zurückgibt
        when(ticketService.createTicket(any(CreateTicketDTO.class))).thenReturn(testTicketDTO);

        mockMvc.perform(post("/api/v1/tickets/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk()) // Erwartet 200 OK, da die Methode TicketDTO zurückgibt
                .andExpect(jsonPath("$.ticketKey").value("TPJ-1"));
    }

    @Test
    @DisplayName("PUT /api/v1/tickets/1/update should return 200 OK and updated ticket")
    @WithMockUser(username = "testuser", authorities = "TICKET_UPDATE")
    void updateTicket_shouldReturnOk_andUpdatedTicket() throws Exception {
        when(customSecurityService.canUpdateTicket(eq(1L), any())).thenReturn(true);
        when(ticketService.updateTicket(eq(1L), any(TicketDTO.class))).thenReturn(testTicketDTO);

        mockMvc.perform(put("/api/v1/tickets/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTicketDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/tickets/1/delete should return 200 OK")
    @WithMockUser(authorities = "TICKET_DELETE")
    void deleteTicket_shouldReturnOk() throws Exception {
        doNothing().when(ticketService).deleteTicket(1L);

        mockMvc.perform(delete("/api/v1/tickets/1/delete")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // --- Error and Validation Tests (4xx) ---

    @Test
    @DisplayName("GET /api/v1/tickets/99 should return 404 if not found")
    @WithMockUser(authorities = "TICKET_READ_ALL")
    void getTicketById_shouldReturn404_ifNotFound() throws Exception {
        when(ticketService.getTicketById(99L)).thenThrow(new ResourceNotFoundException("Ticket not found"));

        mockMvc.perform(get("/api/v1/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/0 should return 400 for invalid path variable")
    @WithMockUser(authorities = "TICKET_READ_ALL")
    void getTicketById_shouldReturn400_forInvalidId() throws Exception {
        // Die @Min(1) Validierung sollte fehlschlagen
        mockMvc.perform(get("/api/v1/tickets/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/tickets/create should return 400 for invalid DTO")
    @WithMockUser(authorities = "TICKET_CREATE")
    void createTicket_shouldReturn400_forInvalidDTO() throws Exception {
        // Titel ist leer, Status ist null -> verletzt @NotBlank und @NotNull
        CreateTicketDTO badDTO = new CreateTicketDTO("", "Desc", null, null, null);

        mockMvc.perform(post("/api/v1/tickets/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be empty"))
                .andExpect(jsonPath("$.fieldErrors.status").value("A status must be selected"));
    }

    @Test
    @DisplayName("PUT /api/v1/tickets/1/update should return 400 for ID mismatch")
    @WithMockUser(username = "testuser", authorities = "TICKET_UPDATE")
    void updateTicket_shouldReturn400_forIdMismatch() throws Exception {
        when(customSecurityService.canUpdateTicket(eq(1L), any())).thenReturn(true);

        // DTO hat ID 2L, aber Path Variable ist 1L
        TicketDTO mismatchDTO = new TicketDTO(
                2L, "TPJ-2", "Other Ticket", "Desc",
                TicketStatus.CREATED, testUserDTO, testProjectDTO,
                Instant.now(), Instant.now(), testUserDTO, testUserDTO
        );

        // Service wird nicht aufgerufen, der Controller wirft MismatchedIdException
        mockMvc.perform(put("/api/v1/tickets/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchDTO)))
                .andExpect(status().isBadRequest()) // MismatchedIdException wird zu 400
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Path variable 'id' = 1 does not match 'id' = 2in request body"));
    }
}