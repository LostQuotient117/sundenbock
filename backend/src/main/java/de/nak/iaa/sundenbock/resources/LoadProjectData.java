package de.nak.iaa.sundenbock.resources;

import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class LoadProjectData {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public LoadProjectData(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() == 0) {
            // Dev-User und Admin-User aus LoadSecurityData abrufen
            Optional<User> devUserOpt = userRepository.findByUsername("dev");
            Optional<User> adminUserOpt = userRepository.findByUsername("admin");

            User devUser = devUserOpt.orElse(null);
            User adminUser = adminUserOpt.orElse(null);

            Project p1 = new Project();
            p1.setTitle("Ticket System");
            p1.setDescription("Internes Tool zur Verwaltung und Nachverfolgung von Fehlern und Feature-Requests.");
            p1.setCreatedOn(LocalDateTime.now().minusDays(10));
            p1.setCreatedBy(adminUser);

            Project p2 = new Project();
            p2.setTitle("Customer Portal");
            p2.setDescription("Web-Portal, das Kunden erlaubt, Bestellungen und Supportanfragen einzusehen und zu verwalten.");
            p2.setCreatedOn(LocalDateTime.now().minusDays(20));
            p2.setCreatedBy(devUser);

            Project p3 = new Project();
            p3.setTitle("Data Analytics Platform");
            p3.setDescription("Interne Plattform zur Analyse und Visualisierung von Geschäftsdaten für das Management.");
            p3.setCreatedOn(LocalDateTime.now().minusDays(30));
            p3.setCreatedBy(adminUser);

            projectRepository.save(p1);
            projectRepository.save(p2);
            projectRepository.save(p3);

            System.out.println("Example projects created!");
        }
    }
}
