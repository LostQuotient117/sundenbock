package de.nak.iaa.sundenbock.service.security;


import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;


@Component("customSecurityService")
public class CustomSecurityService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public CustomSecurityService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    public boolean isCommentOwner(Long commentId, Authentication authentication) {
        if (authentication == null) return false;

        return commentRepository.findById(commentId)
                .map(comment -> comment.getCreatedBy().getUsername().equals(authentication.getName()))
                .orElse(false);
    }

    public boolean canAccessTicket(Long ticketId, Authentication authentication) {
        if (authentication == null) return false;

        return ticketRepository.findById(ticketId)
                .map(ticket -> {
                    String currentUsername = authentication.getName();
                    boolean isCreator = ticket.getCreatedBy().getUsername().equals(currentUsername);
                    boolean isResponsible = ticket.getResponsiblePerson().getUsername().equals(currentUsername);
                    return isCreator || isResponsible;
                })
                .orElse(false);
    }

    public boolean canUpdateTicket(Long ticketId, Authentication authentication) {
        if (authentication == null) return false;

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TICKET_UPDATE"))) {
            return true;
        }

        return canAccessTicket(ticketId, authentication);
    }

    public boolean canAccessUser(String username, Authentication authentication) {
        if (authentication == null) return false;

        if (hasAuthority(authentication, "USER_MANAGE")) {
            return true;
        }

        return authentication.getName().equals(username);
    }

    private boolean hasAuthority(Authentication authentication, String authorityName) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(authorityName));
    }
}
