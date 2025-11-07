package de.nak.iaa.sundenbock.model.ticket;

import java.util.EnumSet;
import java.util.Set;
/**
 * Workflow states for a {@link de.nak.iaa.sundenbock.model.ticket.Ticket}.
 * <p>
 * These values represent the ticket lifecycle used by business logic and the UI.
 * Typical semantics:
 * <ul>
 *   <li>{@link #CREATED}: Newly created, not yet triaged.</li>
 *   <li>{@link #REOPENED}: Reopened after being resolved or closed.</li>
 *   <li>{@link #IN_PROGRESS}: Actively being worked on.</li>
 *   <li>{@link #RESOLVED}: Fix/solution implemented, awaiting verification.</li>
 *   <li>{@link #REJECTED}: Won't fix or invalid.</li>
 *   <li>{@link #CLOSED}: Verified and finalized; no further work planned.</li>
 * </ul>
 * </p>
 */
public enum TicketStatus {
    CREATED,
    REOPENED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED,
    CLOSED;

    public Set<TicketStatus> getAllowedTransitionsForDeveloper() {
        return switch (this) {
            case CREATED, REOPENED -> EnumSet.of(IN_PROGRESS);
            case IN_PROGRESS -> EnumSet.of(RESOLVED, REJECTED);
            default -> EnumSet.noneOf(TicketStatus.class);
        };
    }

    public Set<TicketStatus> getAllowedTransitionsForAuthor() {
        return switch (this) {
            case RESOLVED, REJECTED -> EnumSet.of(REOPENED, CLOSED);
            default -> EnumSet.noneOf(TicketStatus.class);
        };
    }
}
