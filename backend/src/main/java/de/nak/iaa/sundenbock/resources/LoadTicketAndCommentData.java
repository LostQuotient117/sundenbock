package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.comment.Comment;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.CommentRepository;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final Random rand = new Random();

    public LoadTicketAndCommentData(TicketRepository ticketRepository, UserRepository userRepository, ProjectRepository projectRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void run() {
        if (ticketRepository.count() == 0) {
            User user = userRepository.findByUsername("OG-Developer")
                    .orElseThrow(() -> new RuntimeException("user 'OG-Developer' not found for sample data"));

            Project project1 = projectRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("project with id 1 not found for sample data"));

            Project project2 = projectRepository.findById(2L)
                    .orElseThrow(() -> new RuntimeException("project with id 2 not found for sample data"));

            for (int i = 1; i <= 20; i++) {
                createTicketWithComments(project1, user, i);
            }

            for (int i = 1; i <= 10; i++) {
                createTicketWithComments(project2, user, i);
            }

            System.out.println("Example tickets and comments created!");
        }
    }

    private void createTicketWithComments(Project project, User user, int index) {
        Ticket ticket = new Ticket();
        ticket.setTitle("Test Ticket " + project.getAbbreviation() + " #" + index);
        ticket.setDescription("Dies ist eine Testbeschreibung für Ticket #" + index + " im Projekt " + project.getTitle());
        ticket.setStatus(TicketStatus.values()[rand.nextInt(TicketStatus.values().length)]);
        ticket.setResponsiblePerson(user);
        ticket.setProject(project);

        ticket = saveWithTicketKey(ticket);

        List<Comment> commentsToSave = new ArrayList<>();

        Comment parentComment1 = new Comment();
        parentComment1.setTicket(ticket);
        parentComment1.setCommentText("Erster Hauptkommentar für Ticket " + ticket.getTicketKey());
        parentComment1.setLikes(rand.nextInt(10));
        parentComment1.setDislikes(rand.nextInt(2));
        commentsToSave.add(parentComment1);

        Comment parentComment2 = new Comment();
        parentComment2.setTicket(ticket);
        parentComment2.setCommentText("Zweiter Hauptkommentar für Ticket " + ticket.getTicketKey());
        parentComment2.setLikes(rand.nextInt(8));
        parentComment2.setDislikes(rand.nextInt(3));
        commentsToSave.add(parentComment2);

        Comment subComment = new Comment();
        subComment.setTicket(ticket);
        subComment.setCommentText("Unterkommentar zu " + ticket.getTicketKey());
        subComment.setLikes(rand.nextInt(5));
        subComment.setDislikes(rand.nextInt(1));
        subComment.setParentComment(parentComment1); // Verknüpft mit dem ersten Oberkommentar
        commentsToSave.add(subComment);

        commentRepository.saveAll(commentsToSave);
    }

    private Ticket saveWithTicketKey(Ticket ticket) {
        ticket.setTicketKey(ticket.getProject().getAbbreviation() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        Ticket saved = ticketRepository.save(ticket);
        saved.setTicketKey(saved.getProject().getAbbreviation() + "-" + saved.getId());
        return ticketRepository.save(saved);
    }
}
