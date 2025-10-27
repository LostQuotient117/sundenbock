package de.nak.iaa.sundenbock.ticket.comment;

import de.nak.iaa.sundenbock.ticket.Ticket;
import de.nak.iaa.sundenbock.ticket.TicketRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketid}/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public CommentController(CommentRepository commentRepository, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }
    //TODO: Implement

    // Endpoint: Retrieve all top-level comments for a ticket
    // GET /api/tickets/{ticketId}/comments
//    @GetMapping
//    public List<Comment> getCommentsByTicket(@PathVariable Long ticketId) {
//         return commentRepository.findByTicketIdAndParentCommentIsNull(ticketId)
//    }

    //TODO: Implement

    // Endpoint: Einen neuen Top-Level-Comment erstellen
    // POST /api/tickets/{ticketId}/comments
//    @PostMapping
//    public Comment createComment(@PathVariable Long ticketId, @RequestBody Comment comment) {
//        Ticket ticket = ticketRepository.findById(ticketId)
//                .orElseThrow(() -> new RuntimeException("Ticket nicht gefunden"));
//        comment.setTicket(ticket);
//        return commentRepository.save(comment);
//    }

    //TODO: Implement

    // Endpoint: Einen Sub-Comment zu einem Comment erstellen
    // POST /api/tickets/{ticketId}/comments/{commentId}/subcomments
//    @PostMapping("/{commentId}/subcomments")
//    public Comment createSubComment(
//            @PathVariable Long ticketId,
//            @PathVariable Long commentId,
//            @RequestBody Comment subComment) {
//        Comment parentComment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new RuntimeException("Comment nicht gefunden"));
//        subComment.setTicket(parentComment.getTicket()); // Gleiche Ticket-Zuordnung
//        subComment.setParentComment(parentComment);
//        return commentRepository.save(subComment);
//    }

    //TODO: Implement

    // Endpoint: Alle Sub-Comments eines Comments abrufen
    // GET /api/tickets/{ticketId}/comments/{commentId}/subcomments
//    @GetMapping("/{commentId}/subcomments")
//    public List<Comment> getSubComments(
//            @PathVariable Long ticketId,
//            @PathVariable Long commentId) {
//        return commentRepository.findByParentCommentId(commentId);
//    }

    //TODO: Implement

    // Endpoint: Update a comment
    // PUT /api/tickets/{ticketId}/comments/{commentId}
//    @PutMapping("/{commentId}")
//    public Comment updateComment(
//            @PathVariable Long ticketId,
//            @PathVariable Long commentId,
//            @RequestBody Comment commentDetails) {
//        Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() -> new RuntimeException("Comment nicht gefunden"));
//        comment.setText(commentDetails.getText()); // Beispiel: Nur Text aktualisieren
//        return commentRepository.save(comment);
//    }

    // Endpoint: Delete a comment
    // DELETE /api/tickets/{ticketId}/comments/{commentId}
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
