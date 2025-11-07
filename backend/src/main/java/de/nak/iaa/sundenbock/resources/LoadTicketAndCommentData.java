package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Component responsible for loading example tickets and comments into the database.
 * <p>
 * Checks if any tickets exist and, if none are found, creates and saves
 * example tickets along with associated comments and sub-comments.
 * </p>
 */
@Component
class LoadTicketAndCommentData {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;

    public LoadTicketAndCommentData(TicketRepository ticketRepository, UserRepository userRepository, ProjectRepository projectRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void run() {
        if (ticketRepository.count() == 0) {
            Ticket t1 = new Ticket();
            t1.setTitle("500 error on login page");
            t1.setDescription("Login page throws 500 error");
            t1.setStatus(TicketStatus.CREATED);
            t1.setResponsiblePerson(userRepository.findByUsername("OG-Developer").orElseThrow(() -> new RuntimeException("user not found for sample data")));
            t1.setProject(projectRepository.findByTitle("Ticket System").orElseThrow(() -> new RuntimeException("project not found for sample data")));

            Ticket t2 = new Ticket();
            t2.setTitle("CSS misalignment: Dashboard");
            t2.setDescription("CSS misalignment on dashboard");
            t2.setStatus(TicketStatus.IN_PROGRESS);
            t2.setResponsiblePerson(userRepository.findByUsername("OG-Developer").orElseThrow(() -> new RuntimeException("user not found for sample data")));
            t2.setProject(projectRepository.findByTitle("Ticket System").orElseThrow(() -> new RuntimeException("project not found for sample data")));

            t1 = saveWithTicketKey(t1);
            t2 = saveWithTicketKey(t2);

            Comment c1 = new Comment();
            c1.setTicket(t1);
            c1.setCommentText("Ich habe den Fehler reproduziert. Es scheint ein Problem mit der Datenbankverbindung zu geben.");
            c1.setLikes(2);
            c1.setDislikes(0);

            // Sub-Comment zu c1
            Comment c1_sub1 = new Comment();
            c1_sub1.setTicket(t1);
            c1_sub1.setParentComment(c1);
            c1_sub1.setCommentText("Kannst du bitte den genauen Fehler-Log posten?");
            c1_sub1.setLikes(1);
            c1_sub1.setDislikes(0);

            // Kommentar zu Ticket t2
            Comment c2 = new Comment();
            c2.setTicket(t2);
            c2.setCommentText("Ich habe das CSS-Problem analysiert. Es liegt an der falschen Grid-Konfiguration.");
            c2.setLikes(5);
            c2.setDislikes(0);

            // Sub-Comment zu c2
            Comment c2_sub1 = new Comment();
            c2_sub1.setTicket(t2);
            c2_sub1.setParentComment(c2);
            c2_sub1.setCommentText("Kannst du einen Fix vorschlagen?");
            c2_sub1.setLikes(3);
            c2_sub1.setDislikes(0);

            // Kommentare speichern
            commentRepository.save(c1);
            commentRepository.save(c1_sub1);
            commentRepository.save(c2);
            commentRepository.save(c2_sub1);


            System.out.println("Example tickets created!");
        }
    }
    private Ticket saveWithTicketKey(Ticket ticket) {
        // Temporärer Key, falls nullable=false in der DB
        ticket.setTicketKey(ticket.getProject().getAbbreviation() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        Ticket saved = ticketRepository.save(ticket);
        // Finalen Key mit DB-ID setzen
        saved.setTicketKey(saved.getProject().getAbbreviation() + "-" + saved.getId());
        return ticketRepository.save(saved);
    }
}
