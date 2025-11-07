package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.mapper.TicketMapper;
import de.nak.iaa.sundenbock.dto.ticketDTO.CreateTicketDTO;
import de.nak.iaa.sundenbock.dto.ticketDTO.TicketDTO;
import de.nak.iaa.sundenbock.dto.userDTO.UserDTO;
import de.nak.iaa.sundenbock.dto.projectDTO.ProjectDTO;
import de.nak.iaa.sundenbock.exception.ResourceNotFoundException;
import de.nak.iaa.sundenbock.model.project.Project;
import de.nak.iaa.sundenbock.model.ticket.Ticket;
import de.nak.iaa.sundenbock.model.ticket.TicketStatus;
import de.nak.iaa.sundenbock.model.user.User;
import de.nak.iaa.sundenbock.repository.ProjectRepository;
import de.nak.iaa.sundenbock.repository.TicketRepository;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private TicketDTO ticketDTO;
    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("user1");
        user.setFirstName("Test");
        user.setLastName("User");

        project = new Project();
        project.setTitle("Demo Project");

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.CREATED);
        ticket.setResponsiblePerson(user);
        ticket.setProject(project);

        UserDTO userDTO = new UserDTO(1L, "user1", "Test", "User");
        ProjectDTO projectDTO = new ProjectDTO(1L, "Demo Project", "Demo Project for Mockito", Instant.now(), Instant.now(), userDTO, userDTO);
        ticketDTO = new TicketDTO(1L, "Test Ticket", "Description", TicketStatus.CREATED, userDTO, projectDTO, Instant.now(), Instant.now(), userDTO, userDTO
        );
    }

    @Test
    void getTicketById_returnsDTO_whenFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toTicketDTO(ticket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.getTicketById(1L);

        assertEquals(ticketDTO, result);
        verify(ticketRepository).findById(1L);
        verify(ticketMapper).toTicketDTO(ticket);
    }

    @Test
    void getTicketById_throwsException_whenNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.getTicketById(1L));

        verify(ticketRepository).findById(1L);
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void createTicket_returnsMappedDTO_whenSuccessful() {
        CreateTicketDTO createDTO = new CreateTicketDTO("New Ticket", "Description", TicketStatus.CREATED, "user1", 1L);

        Ticket mappedTicket = new Ticket();
        Ticket savedTicket = new Ticket();

        when(ticketMapper.toTicketFromCreate(createDTO)).thenReturn(mappedTicket);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(ticketRepository.save(mappedTicket)).thenReturn(savedTicket);
        when(ticketMapper.toTicketDTO(savedTicket)).thenReturn(ticketDTO);

        TicketDTO result = ticketService.createTicket(createDTO);

        assertEquals(ticketDTO, result);
        verify(ticketRepository).save(mappedTicket);
    }

    @Test
    void createTicket_throwsException_whenUserNotFound() {
        CreateTicketDTO createDTO = new CreateTicketDTO("Test", "Desc", TicketStatus.CREATED, "missing", 10L);
        when(ticketMapper.toTicketFromCreate(createDTO)).thenReturn(new Ticket());
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.createTicket(createDTO));
    }

    @Test
    void deleteTicket_deletes_whenExists() {
        when(ticketRepository.existsById(1L)).thenReturn(true);

        ticketService.deleteTicket(1L);

        verify(ticketRepository).deleteById(1L);
    }

    @Test
    void deleteTicket_throwsException_whenNotExists() {
        when(ticketRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> ticketService.deleteTicket(1L));

        verify(ticketRepository, never()).deleteById(any());
    }

    @Test
    void search_returnsPage() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), any(Pageable.class))).thenReturn(page);

        Page<Ticket> result = ticketService.search("test", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(ticketRepository).findAll(ArgumentMatchers.<Specification<Ticket>>any(), any(Pageable.class));
    }
}
