package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.commentDTO.CommentDTO;
import de.nak.iaa.sundenbock.dto.commentDTO.CreateCommentDTO;
import de.nak.iaa.sundenbock.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDTO> getCommentsByTicket(@PathVariable @Min(1) Long ticketId) {
         return commentService.getCommentsByTicketId(ticketId);
    }

    @PostMapping("/create")
    public CommentDTO createComment(@Valid @RequestBody CreateCommentDTO createCommentDTO) {
        return commentService.createComment(createCommentDTO);
    }

    @PutMapping("/{commentId}/update")
    public CommentDTO updateComment(@Valid @RequestBody CommentDTO commentDTO){
        return commentService.updateComment(commentDTO);
    }

    @DeleteMapping("/{commentId}/delete")
    public void deleteComment(@PathVariable @Min(1) Long commentId) {
        commentService.deleteCommentWithChildren(commentId);
    }
}
