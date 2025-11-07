package de.nak.iaa.sundenbock.controller;

import de.nak.iaa.sundenbock.dto.PageDTO;
import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.pageable.PageableFactory;
import de.nak.iaa.sundenbock.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private PageableFactory pageableFactory;

    @InjectMocks
    private TicketController ticketController;

    private Ticket ticket;
    private TicketDTO ticketDTO;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("user1");

        Project project = new Project();
        project.setTitle("Demo Project");

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setResponsiblePerson(user);
        ticket.setProject(project);

        ticketDTO = new TicketDTO(
                1L,
                "Test Ticket",
                "Description",
                TicketStatus.CREATED,
                null, // UserDTO placeholder
                null, // ProjectDTO placeholder
                Instant.now(),
                Instant.now(),
                null,
                null
        );
    }

    @Test
    void getTicketById_returnsDTO_whenFound() {
        when(ticketService.getTicketById(1L)).thenReturn(ticketDTO);

        TicketDTO result = ticketController.getTicketById(1L);

        assertEquals(ticketDTO, result);
        verify(ticketService).getTicketById(1L);
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void createTicket_returnsDTO_whenSuccessful() {
        CreateTicketDTO createDTO = new CreateTicketDTO("New Ticket", "Description", TicketStatus.CREATED, "user1", 1L);

        when(ticketService.createTicket(createDTO)).thenReturn(ticketDTO);

        TicketDTO result = ticketController.createTicket(createDTO);

        assertEquals(ticketDTO, result);
        verify(ticketService).createTicket(createDTO);
    }

    @Test
    void deleteTicket_callsService() {
        ticketController.deleteTicket(1L);

        verify(ticketService).deleteTicket(1L);
    }

    @Test
    void getTickets_returnsPageDTO() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(pageableFactory.createPageable(0, 20, null, Set.of("createdDate","lastModifiedDate","title","status", "createdBy.username"), Map.of("createdOn","createdDate","creator", "createdBy.username")))
                .thenReturn(PageRequest.of(0, 20));

        when(ticketService.search(null, PageRequest.of(0, 20))).thenReturn(page);
        when(ticketMapper.toTicketDTOs(page.getContent())).thenReturn(List.of(ticketDTO));

        PageDTO<TicketDTO> result = ticketController.getTickets(null, 0, 20, null);

        assertEquals(1, result.items().size());
        verify(ticketService).search(null, PageRequest.of(0, 20));
        verify(ticketMapper).toTicketDTOs(page.getContent());
    }

    @Test
    void updateTicket_callsService_whenIdsMatch() {
        TicketDTO updateDTO = new TicketDTO(
                1L,
                "Updated Title",
                "Updated Description",
                TicketStatus.CREATED,
                null,
                null,
                Instant.now(),
                Instant.now(),
                null,
                null
        );

        when(ticketService.updateTicket(1L, updateDTO)).thenReturn(updateDTO);

        TicketDTO result = ticketController.updateTicket(1L, updateDTO);

        assertEquals(updateDTO, result);
        verify(ticketService).updateTicket(1L, updateDTO);
    }

    // Optional: Test für MismatchedIdException
    @Test
    void updateTicket_throwsMismatchedIdException_whenIdsMismatch() {
        TicketDTO updateDTO = new TicketDTO(
                2L,
                "Updated Title",
                "Updated Description",
                TicketStatus.CREATED,
                null,
                null,
                Instant.now(),
                Instant.now(),
                null,
                null
        );

        try {
            ticketController.updateTicket(1L, updateDTO);
        } catch (Exception e) {
            assertEquals("Path variable 'id' = 1 does not match 'id' = 2in request body", e.getMessage());
        }
    }
}
