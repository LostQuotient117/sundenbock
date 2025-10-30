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

    @GetMapping("/all-comments")
    public List<CommentDTO> getCommentsByTicket(@PathVariable Long ticketid) {
         return commentService.getCommentsByTicketId(ticketid);
    }

    @PostMapping
    public CommentDTO createComment(@RequestBody CommentDTO commentDTO) {
        return commentService.createComment(commentDTO);
    }

    @PutMapping("/{commentId}")
    public CommentDTO updateComment(CommentDTO commentDTO){
        return commentService.updateComment(commentDTO);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
