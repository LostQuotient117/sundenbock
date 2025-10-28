package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:h2:mem:mydb")
public class LoadExampleData implements CommandLineRunner {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public LoadExampleData(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
            if (ticketRepository.count() == 0) {
                Ticket t1 = new Ticket();
                t1.setDescription("Login page throws 500 error");
                t1.setStatus(TicketStatus.CREATED);
                t1.setCreatedOn(LocalDateTime.now());
                t1.setLastChange(LocalDateTime.now());
                t1.setResponsiblePerson("Max Mustermann");
                t1.setAuthor("Jannick Gottschalk");
                t1.setProject("TS");

                Ticket t2 = new Ticket();
                t2.setDescription("CSS misalignment on dashboard");
                t2.setStatus(TicketStatus.IN_PROGRESS);
                t2.setCreatedOn(LocalDateTime.now().minusDays(1));
                t2.setLastChange(LocalDateTime.now());
                t2.setResponsiblePerson("Lisa Entwickler");
                t2.setAuthor("Jannick Gottschalk");
                t2.setProject("TS");

                ticketRepository.save(t1);
                ticketRepository.save(t2);

                Comment c1 = new Comment();
                c1.setTicket(t1);
                c1.setAuthor("Max Mustermann");
                c1.setCreatedOn(LocalDateTime.now());
                c1.setLastChange(LocalDateTime.now()); // musste ich adden weil sonst crash
                c1.setCommentText("Ich habe den Fehler reproduziert. Es scheint ein Problem mit der Datenbankverbindung zu geben.");
                c1.setLikes(2);
                c1.setDislikes(0);

                // Sub-Comment zu c1
                Comment c1_sub1 = new Comment();
                c1_sub1.setTicket(t1);
                c1_sub1.setParentComment(c1);
                c1_sub1.setAuthor("Jannick Gottschalk");
                c1_sub1.setCreatedOn(LocalDateTime.now().plusMinutes(10));
                c1_sub1.setLastChange(LocalDateTime.now().plusMinutes(10)); // musste ich adden weil sonst crash
                c1_sub1.setCommentText("Kannst du bitte den genauen Fehler-Log posten?");
                c1_sub1.setLikes(1);
                c1_sub1.setDislikes(0);

                // Kommentar zu Ticket t2
                Comment c2 = new Comment();
                c2.setTicket(t2);
                c2.setAuthor("Lisa Entwickler");
                c2.setCreatedOn(LocalDateTime.now().minusHours(2));
                c2.setLastChange(LocalDateTime.now().minusHours(2)); // musste ich adden weil sonst crash
                c2.setCommentText("Ich habe das CSS-Problem analysiert. Es liegt an der falschen Grid-Konfiguration.");
                c2.setLikes(5);
                c2.setDislikes(0);

                // Sub-Comment zu c2
                Comment c2_sub1 = new Comment();
                c2_sub1.setTicket(t2);
                c2_sub1.setParentComment(c2);
                c2_sub1.setAuthor("Jannick Gottschalk");
                c2_sub1.setCreatedOn(LocalDateTime.now().minusHours(1));
                c2_sub1.setLastChange(LocalDateTime.now().minusHours(1)); // musste ich adden weil sonst crash
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
}
