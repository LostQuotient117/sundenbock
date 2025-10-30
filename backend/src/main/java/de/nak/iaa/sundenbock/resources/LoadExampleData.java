package de.nak.iaa.sundenbock.resources;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:h2:mem:mydb")
@Transactional
public class LoadExampleData implements CommandLineRunner {

    private final LoadBaselineSecurityData loadBaselineSecurityData;
    private final LoadSampleUsersData loadSampleUsersData;
    private final LoadProjectData loadProjectData;
    private final LoadTicketAndCommentData loadTicketAndCommentData;

    public LoadExampleData(LoadBaselineSecurityData loadBaselineSecurityData, LoadSampleUsersData loadSampleUsersData, LoadProjectData loadProjectData, LoadTicketAndCommentData loadTicketAndCommentData) {
        this.loadBaselineSecurityData = loadBaselineSecurityData;
        this.loadSampleUsersData = loadSampleUsersData;
        this.loadProjectData = loadProjectData;
        this.loadTicketAndCommentData = loadTicketAndCommentData;
    }

    @Override
    public void run(String... args) {
        loadBaselineSecurityData.run(args);
        loadSampleUsersData.run(args);
        loadProjectData.run(args);
        loadTicketAndCommentData.run(args);
    }
}
