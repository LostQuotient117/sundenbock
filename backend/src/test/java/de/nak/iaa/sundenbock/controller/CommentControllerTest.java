package de.nak.iaa.sundenbock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nak.iaa.sundenbock.config.JwtAuthFilter;
import de.nak.iaa.sundenbock.config.SecurityConfig;
import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.exception.CustomAccessDeniedHandler;
import de.nak.iaa.sundenbock.exception.CustomAuthenticationEntryPoint;
import de.nak.iaa.sundenbock.exception.MismatchedIdException;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.service.CommentService;
import de.nak.iaa.sundenbock.service.security.CustomSecurityService;
import de.nak.iaa.sundenbock.service.security.JwtService;
import de.nak.iaa.sundenbock.service.user.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        CustomSecurityService.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        UserDetailsServiceImpl.class
})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private PageableFactory pageableFactory;

    @MockitoBean
    private TicketRepository ticketRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomSecurityService customSecurityService;

    private CommentDTO testCommentDTO;
    private CreateCommentDTO testCreateCommentDTO;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testCommentDTO = new CommentDTO(
                1L, 1L, null, "Test Comment", 0, 0,
                List.of(), Instant.now(), Instant.now(), null, null
        );

        testCreateCommentDTO = new CreateCommentDTO(
                1L, null, "New Comment"
        );

        testPageable = PageRequest.of(0, 20);
        when(pageableFactory.createPageable(eq(0), eq(20), any(), any(), any()))
                .thenReturn(testPageable);
    }

    @Test
    @WithAnonymousUser
    void getPagedComments_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/1/comments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getPagedComments_shouldReturn403_ifNoAccess() throws Exception {
        when(customSecurityService.canAccessTicket(eq(1L), any())).thenReturn(false);
        mockMvc.perform(get("/api/v1/tickets/1/comments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "TICKET_READ_ALL")
    void getPagedComments_shouldReturn200_forAdmin() throws Exception {
        when(ticketRepository.existsById(1L)).thenReturn(true);
        Page<CommentDTO> commentPage = new PageImpl<>(List.of(testCommentDTO), testPageable, 1);
        when(commentService.getPagedCommentsWithReplies(1L, testPageable)).thenReturn(commentPage);

        mockMvc.perform(get("/api/v1/tickets/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1));
    }

    @Test
    @WithMockUser
    void getPagedComments_shouldReturn200_ifCanAccessTicket() throws Exception {
        when(customSecurityService.canAccessTicket(eq(1L), any())).thenReturn(true);
        when(ticketRepository.existsById(1L)).thenReturn(true);
        Page<CommentDTO> commentPage = new PageImpl<>(List.of(testCommentDTO), testPageable, 1);
        when(commentService.getPagedCommentsWithReplies(1L, testPageable)).thenReturn(commentPage);

        mockMvc.perform(get("/api/v1/tickets/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].commentText").value("Test Comment"));
    }

    @Test
    @WithMockUser(authorities = "TICKET_READ_ALL")
    void getPagedComments_shouldReturn404_ifTicketNotFound() throws Exception {
        when(ticketRepository.existsById(1L)).thenReturn(false);
        when(commentService.getPagedCommentsWithReplies(1L, testPageable))
                .thenThrow(new ResourceNotFoundException("Associated ticket not found"));

        mockMvc.perform(get("/api/v1/tickets/1/comments"))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithAnonymousUser
    void createComment_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/tickets/1/comments/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateCommentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createComment_shouldReturn403_ifNoAccess() throws Exception {
        when(customSecurityService.canAccessTicket(eq(1L), any())).thenReturn(false);
        mockMvc.perform(post("/api/v1/tickets/1/comments/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateCommentDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "COMMENT_CREATE")
    void createComment_shouldReturn200_withPermission() throws Exception {
        when(commentService.createComment(any(CreateCommentDTO.class))).thenReturn(testCommentDTO);

        mockMvc.perform(post("/api/v1/tickets/1/comments/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void createComment_shouldReturn400_forInvalidDTO() throws Exception {
        CreateCommentDTO badDTO = new CreateCommentDTO(1L, null, "");
        mockMvc.perform(post("/api/v1/tickets/1/comments/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.commentText").value("The comment needs a body text"));
    }

    @Test
    @WithMockUser(authorities = "COMMENT_CREATE")
    void createComment_shouldReturn400_forIdMismatch() throws Exception {
        CreateCommentDTO mismatchDTO = new CreateCommentDTO(2L, null, "Text");
        when(commentService.createComment(any(CreateCommentDTO.class)))
                .thenThrow(new MismatchedIdException("Path variable 'ticketId' = 1does not match 'ticketId = 2 in request body"));

        mockMvc.perform(post("/api/v1/tickets/1/comments/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path variable 'ticketId' = 1does not match 'ticketId = 2 in request body"));
    }

    @Test
    @WithAnonymousUser
    void updateComment_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(put("/api/v1/tickets/1/comments/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void updateComment_shouldReturn403_ifNotOwner() throws Exception {
        when(customSecurityService.isCommentOwner(eq(1L), any())).thenReturn(false);
        mockMvc.perform(put("/api/v1/tickets/1/comments/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateComment_shouldReturn200_ifOwner() throws Exception {
        when(customSecurityService.isCommentOwner(eq(1L), any())).thenReturn(true);
        when(commentService.updateComment(any(CommentDTO.class))).thenReturn(testCommentDTO);

        mockMvc.perform(put("/api/v1/tickets/1/comments/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "COMMENT_UPDATE")
    void updateComment_shouldReturn200_withPermission() throws Exception {
        when(commentService.updateComment(any(CommentDTO.class))).thenReturn(testCommentDTO);

        mockMvc.perform(put("/api/v1/tickets/1/comments/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(authorities = "COMMENT_UPDATE")
    void updateComment_shouldReturn400_forIdMismatch() throws Exception {
        CommentDTO mismatchDTO = new CommentDTO(2L, 1L, null, "Text", 0, 0, List.of(), null, null, null, null);

        mockMvc.perform(put("/api/v1/tickets/1/comments/1/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Path variable 'commentId' = 1 does not match 'id' = 2 in request body"));
    }

    @Test
    @WithAnonymousUser
    void deleteComment_shouldReturn401_forAnonymous() throws Exception {
        mockMvc.perform(delete("/api/v1/tickets/1/comments/1/delete")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deleteComment_shouldReturn403_ifNotOwner() throws Exception {
        when(customSecurityService.isCommentOwner(eq(1L), any())).thenReturn(false);
        mockMvc.perform(delete("/api/v1/tickets/1/comments/1/delete")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deleteComment_shouldReturn200_ifOwner() throws Exception {
        when(customSecurityService.isCommentOwner(eq(1L), any())).thenReturn(true);
        doNothing().when(commentService).deleteCommentWithChildren(1L, 1L);

        mockMvc.perform(delete("/api/v1/tickets/1/comments/1/delete")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "COMMENT_DELETE")
    void deleteComment_shouldReturn200_withPermission() throws Exception {
        doNothing().when(commentService).deleteCommentWithChildren(1L, 1L);

        mockMvc.perform(delete("/api/v1/tickets/1/comments/1/delete")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "COMMENT_DELETE")
    void deleteComment_shouldReturn400_forInvalidId() throws Exception {
        mockMvc.perform(delete("/api/v1/tickets/1/comments/0/delete")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}