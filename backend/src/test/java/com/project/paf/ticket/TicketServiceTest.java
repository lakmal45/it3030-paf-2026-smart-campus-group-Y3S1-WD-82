package com.project.paf.ticket;

import com.project.paf.modules.auditlog.AuditLogService;
import com.project.paf.modules.resource.exception.ResourceNotFoundException;
import com.project.paf.modules.user.model.Role;
import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.repository.UserRepository;
import com.project.paf.modules.notification.service.AppNotificationService;
import com.project.paf.modules.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketService.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketCommentRepository commentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EmailService emailService;
    
    @Mock
    private AppNotificationService appNotificationService;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TicketService ticketService;

    private User adminUser;
    private User regularUser;
    private User technicianUser;
    private IncidentTicket openTicket;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@smartcampus.com");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setEmail("user@smartcampus.com");
        regularUser.setRole(Role.USER);

        technicianUser = new User();
        technicianUser.setId(3L);
        technicianUser.setEmail("tech@smartcampus.com");
        technicianUser.setRole(Role.TECHNICIAN);

        openTicket = new IncidentTicket();
        openTicket.setId(10L);
        openTicket.setCreatedBy(regularUser);
        openTicket.setStatus(TicketStatus.OPEN);
        openTicket.setImageUrls(new ArrayList<>());
    }

    @Test
    void createTicket_ShouldSetStatusToOpen() {
        log.info("Running createTicket_ShouldSetStatusToOpen...");
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCategory("PLUMBING");
        request.setDescription("Leaking pipe");
        request.setPriority("HIGH");

        when(ticketRepository.save(any(IncidentTicket.class))).thenAnswer(invocation -> {
            IncidentTicket saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        TicketResponse response = ticketService.createTicket(request, regularUser);
        
        assertNotNull(response);
        assertEquals(TicketStatus.OPEN, response.getStatus());
        verify(ticketRepository, times(1)).save(any(IncidentTicket.class));
        verify(emailService, times(1)).notifyTicketCreated(any());
        verify(emailService, times(1)).notifyAdminsAndManagersNewTicket(any(), any());
    }

    @Test
    void updateTicketStatus_ValidTransition_ShouldSucceed() {
        log.info("Running updateTicketStatus_ValidTransition_ShouldSucceed...");
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any(IncidentTicket.class))).thenReturn(openTicket);

        TicketResponse response = ticketService.updateTicketStatus(10L, request, adminUser);
 
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
        verify(ticketRepository, times(1)).save(openTicket);
        verify(emailService, times(1)).notifyStatusChange(any(), any());
    }

    @Test
    void updateTicketStatus_InvalidTransition_ShouldThrowIllegalStateException() {
        log.info("Running updateTicketStatus_InvalidTransition_ShouldThrowIllegalStateException...");
        openTicket.setStatus(TicketStatus.CLOSED);

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));

        assertThrows(IllegalStateException.class, () -> ticketService.updateTicketStatus(10L, request, adminUser));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void updateTicketStatus_ByUserRole_ShouldThrowAccessDeniedException() {
        log.info("Running updateTicketStatus_ByUserRole_ShouldThrowAccessDeniedException...");
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        assertThrows(AccessDeniedException.class, () -> ticketService.updateTicketStatus(10L, request, regularUser));
    }

    @Test
    void assignTechnician_ShouldSetStatusToInProgress_WhenCurrentlyOpen() {
        log.info("Running assignTechnician_ShouldSetStatusToInProgress_WhenCurrentlyOpen...");
        
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(technicianUser));
        when(ticketRepository.save(any(IncidentTicket.class))).thenReturn(openTicket);

        TicketResponse response = ticketService.assignTechnician(10L, 3L, adminUser);
 
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
        assertEquals(technicianUser.getId(), response.getAssignedTechnicianId());
        verify(emailService, times(1)).notifyTechnicianAssigned(any(IncidentTicket.class), eq(technicianUser));
    }

    @Test
    void assignTechnician_ByNonAdmin_ShouldThrowAccessDeniedException() {
        log.info("Running assignTechnician_ByNonAdmin_ShouldThrowAccessDeniedException...");

        assertThrows(AccessDeniedException.class, () -> ticketService.assignTechnician(10L, 3L, regularUser));
    }

    @Test
    void uploadImages_ExceedingThreeImages_ShouldThrowIllegalStateException() {
        log.info("Running uploadImages_ExceedingThreeImages_ShouldThrowIllegalStateException...");
        openTicket.getImageUrls().add("url1");
        openTicket.getImageUrls().add("url2");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));

        List<org.springframework.web.multipart.MultipartFile> files = new ArrayList<>();
        files.add(mock(org.springframework.web.multipart.MultipartFile.class));
        files.add(mock(org.springframework.web.multipart.MultipartFile.class));

        assertThrows(IllegalStateException.class, () -> ticketService.uploadImages(10L, files));
        verify(fileStorageService, never()).store(any());
    }

    @Test
    void addComment_ShouldTriggerNotification_WhenCommenterIsNotOwner() {
        log.info("Running addComment_ShouldTriggerNotification_WhenCommenterIsNotOwner...");
        CommentRequest request = new CommentRequest();
        request.setContent("Looking into this");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        
        TicketComment savedComment = new TicketComment();
        savedComment.setId(200L);
        savedComment.setTicket(openTicket);
        savedComment.setAuthor(adminUser);
        when(commentRepository.save(any(TicketComment.class))).thenReturn(savedComment);

        ticketService.addComment(10L, request, adminUser);
 
        verify(emailService, times(1)).notifyCommentAdded(any(), any(), any());
    }

    @Test
    void deleteTicket_ByUnrelatedUser_ShouldThrowAccessDeniedException() {
        log.info("Running deleteTicket_ByUnrelatedUser_ShouldThrowAccessDeniedException...");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));

        // Technician who is NOT the creator and NOT assigned to this ticket should be denied
        assertThrows(AccessDeniedException.class, () -> ticketService.deleteTicket(10L, technicianUser));
    }

    @Test
    void getTicketById_NotFound_ShouldThrowResourceNotFoundException() {
        log.info("Running getTicketById_NotFound_ShouldThrowResourceNotFoundException...");
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicketById(999L));
    }

    @Test
    void editComment_ByNonAuthorNonAdmin_ShouldThrowAccessDeniedException() {
        log.info("Running editComment_ByNonAuthorNonAdmin_ShouldThrowAccessDeniedException...");
        TicketComment comment = new TicketComment();
        comment.setId(50L);
        comment.setAuthor(adminUser); // Author is admin

        CommentRequest request = new CommentRequest();
        request.setContent("New Content");

        when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

        // Regular user trying to edit admin's comment
        assertThrows(AccessDeniedException.class, () -> ticketService.editComment(50L, request, regularUser));
    }
}