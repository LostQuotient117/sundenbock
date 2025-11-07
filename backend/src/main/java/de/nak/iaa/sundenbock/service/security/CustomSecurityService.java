package de.nak.iaa.sundenbock.service.security;


import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;


/**
 * Service providing custom security checks for tickets, comments, and users.
 * <p>
 * Used to determine if a user has the required permissions or ownership
 * to perform certain actions.
 * </p>
 */
@Component("customSecurityService")
public class CustomSecurityService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public CustomSecurityService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Checks if the authenticated user is the owner of a given comment.
     *
     * @param commentId      the ID of the comment
     * @param authentication the current user's authentication object
     * @return true if the user is the creator of the comment, false otherwise
     */
    public boolean isCommentOwner(Long commentId, Authentication authentication) {
        if (authentication == null) return false;

        return commentRepository.findById(commentId)
                .map(comment -> comment.getCreatedBy().getUsername().equals(authentication.getName()))
                .orElse(false);
    }

    /**
     * Checks if the authenticated user can access a given ticket.
     *
     * @param ticketId       the ID of the ticket
     * @param authentication the current user's authentication object
     * @return true if the user is the creator or responsible person of the ticket
     */
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

    /**
     * Checks if the authenticated user can update a given ticket.
     *
     * @param ticketId       the ID of the ticket
     * @param authentication the current user's authentication object
     * @return true if the user has TICKET_UPDATE authority or access to the ticket
     */
    public boolean canUpdateTicket(Long ticketId, Authentication authentication) {
        if (authentication == null) return false;

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TICKET_UPDATE"))) {
            return true;
        }

        return canAccessTicket(ticketId, authentication);
    }

    /**
     * Checks if the authenticated user can access another user's data.
     *
     * @param username       the username of the target user
     * @param authentication the current user's authentication object
     * @return true if the user has USER_MANAGE authority or is accessing their own data
     */
    public boolean canAccessUser(String username, Authentication authentication) {
        if (authentication == null) return false;

        if (hasAuthority(authentication, "USER_MANAGE")) {
            return true;
        }

        return authentication.getName().equals(username);
    }

    /**
     * Helper method to check if the authentication has a specific authority.
     *
     * @param authentication the current user's authentication object
     * @param authorityName  the authority to check for
     * @return true if the user has the specified authority
     */
    private boolean hasAuthority(Authentication authentication, String authorityName) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(authorityName));
    }
}
