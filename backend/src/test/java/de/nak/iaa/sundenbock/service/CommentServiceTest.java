package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.dto.mapper.CommentMapper;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private CommentService commentService;

    private Ticket ticket;
    private Comment comment1;
    private Comment comment2;
    private CommentDTO commentDTO1;
    private CreateCommentDTO createCommentDTO;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");

        ticket = new Ticket();
        ticket.setId(1L);

        comment1 = new Comment();
        comment1.setId(1L);
        comment1.setCommentText("Test Comment 1");
        comment1.setTicket(ticket);
        comment1.setCreatedBy(user);
        comment1.setLastModifiedBy(user);

        comment2 = new Comment();
        comment2.setId(2L);
        comment2.setCommentText("Test Comment 2 (Reply)");
        comment2.setTicket(ticket);
        comment2.setParentComment(comment1);
        comment1.setChildComments(new ArrayList<>(List.of(comment2)));

        commentDTO1 = new CommentDTO(
                1L, 1L, null, "Test Comment 1", 0, 0,
                new ArrayList<>(), Instant.now(), Instant.now(), null, null
        );

        createCommentDTO = new CreateCommentDTO(
                1L, null, "New Comment Text"
        );
    }

    @Test
    void createComment_shouldCreateTopLevelComment() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        when(commentMapper.toCommentForCreate(createCommentDTO)).thenReturn(comment1);
        when(commentRepository.save(comment1)).thenReturn(comment1);
        when(commentMapper.toCommentDTO(comment1)).thenReturn(commentDTO1);

        CommentDTO result = commentService.createComment(createCommentDTO);

        assertNotNull(result);
        assertEquals(commentDTO1.id(), result.id());
        verify(ticketRepository).findById(1L);
        verify(commentRepository).save(comment1);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void createComment_shouldCreateReplyComment() {
        CreateCommentDTO createReplyDTO = new CreateCommentDTO(1L, 1L, "Reply Text");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        when(commentRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.getReferenceById(1L)).thenReturn(comment1);

        when(commentMapper.toCommentForCreate(createReplyDTO)).thenReturn(comment2);
        when(commentRepository.save(comment2)).thenReturn(comment2);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(commentMapper.toCommentDTO(comment2)).thenReturn(mock(CommentDTO.class));

        commentService.createComment(createReplyDTO);

        verify(commentRepository).save(comment2);
        assertEquals(comment1, comment2.getParentComment());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void createComment_shouldThrowException_whenTicketNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(createCommentDTO));

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_shouldThrowException_whenParentCommentNotFound() {
        CreateCommentDTO createReplyDTO = new CreateCommentDTO(1L, 99L, "Reply Text");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        when(commentRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(createReplyDTO));

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_shouldUpdateComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));
        when(commentMapper.toCommentDTO(comment1)).thenReturn(commentDTO1);
        doNothing().when(commentMapper).updateCommentFromDTO(commentDTO1, comment1);

        CommentDTO result = commentService.updateComment(commentDTO1);

        assertEquals(commentDTO1.id(), result.id());
        verify(commentMapper).updateCommentFromDTO(commentDTO1, comment1);
        verify(commentMapper).toCommentDTO(comment1);
    }

    @Test
    void updateComment_shouldThrowException_whenCommentNotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(commentDTO1));
    }

    @Test
    void deleteCommentWithChildren_shouldDeleteRecursively() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment2));

        when(commentRepository.findChildIdsByParentId(1L)).thenReturn(List.of(2L));
        when(commentRepository.findChildIdsByParentId(2L)).thenReturn(List.of());

        doNothing().when(commentRepository).deleteByIdQuery(anyLong());

        commentService.deleteCommentWithChildren(1L, 1L);

        verify(commentRepository).deleteByIdQuery(1L);
        verify(commentRepository).deleteByIdQuery(2L);
        verify(commentRepository, times(2)).findChildIdsByParentId(anyLong());
    }

    @Test
    void deleteCommentWithChildren_shouldThrowException_whenCommentNotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteCommentWithChildren(1L, 1L));

        verify(commentRepository, never()).deleteByIdQuery(anyLong());
    }

    @Test
    void getPagedCommentsWithReplies_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment1), pageable, 1);

        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTicketIdAndParentCommentIsNull(1L, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentDTO(comment1)).thenReturn(commentDTO1);

        Page<CommentDTO> result = commentService.getPagedCommentsWithReplies(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(commentDTO1.id(), result.getContent().getFirst().id());
        verify(commentRepository).findByTicketIdAndParentCommentIsNull(1L, pageable);
    }

    @Test
    void getPagedCommentsWithReplies_shouldHandleRepliesRecursion() {
        Pageable pageable = PageRequest.of(0, 20);
        comment1.setChildComments(List.of(comment2));
        Page<Comment> commentPage = new PageImpl<>(List.of(comment1), pageable, 1);

        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTicketIdAndParentCommentIsNull(1L, pageable)).thenReturn(commentPage);
        when(commentMapper.toCommentDTO(any(Comment.class))).thenReturn(commentDTO1);

        commentService.getPagedCommentsWithReplies(1L, pageable);

        verify(commentMapper).toCommentDTO(comment1);
    }

    @Test
    void getPagedCommentsWithReplies_shouldThrowException_whenTicketNotFound() {
        Pageable pageable = PageRequest.of(0, 20);
        when(ticketRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.getPagedCommentsWithReplies(1L, pageable));
    }
}