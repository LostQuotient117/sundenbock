package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.CommentDTO;
import de.nak.iaa.sundenbock.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketid}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Endpoint: Retrieve all comments for a ticket
    // GET /api/tickets/{ticketId}/comments
    @GetMapping
    public List<CommentDTO> getCommentsByTicket(@PathVariable Long ticketid) {
         return commentService.getCommentsByTicketId(ticketid);
    }

    // Endpoint: Einen neuen Comment erstellen
    // POST /api/tickets/{ticketId}/comments
    @PostMapping
    public CommentDTO createComment(@RequestBody CommentDTO commentDTO) {
        return commentService.createComment(commentDTO);
    }

    //TODO: Implement in Service

    // Endpoint: Update a comment
    // PUT /api/tickets/{ticketId}/comments/{commentId}
    @PutMapping("/{commentId}")
    public CommentDTO updateComment(CommentDTO commentDTO){
        return commentService.updateComment(commentDTO);
    }


    // Endpoint: Delete a comment
    // DELETE /api/tickets/{ticketId}/comments/{commentId}
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
