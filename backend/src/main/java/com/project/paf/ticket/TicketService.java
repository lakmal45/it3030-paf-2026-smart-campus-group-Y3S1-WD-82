package com.project.paf.ticket;

import com.project.paf.modules.auditlog.AuditAction;
import com.project.paf.modules.auditlog.AuditLogService;
import com.project.paf.modules.resource.exception.ResourceNotFoundException;
import com.project.paf.modules.user.model.Role;
import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.repository.UserRepository;
import com.project.paf.modules.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.lang.NonNull;
import java.util.Objects;
import java.util.List;

/**
 * Business-logic service for the Maintenance & Incident Ticketing System.
 *
 * <p>This service enforces:
 * <ul>
 *   <li>Role-based visibility (USER sees only their own tickets).</li>
 *   <li>Strict status-transition rules.</li>
 *   <li>Comment ownership (only the author or an ADMIN may edit/delete).</li>
 *   <li>Image upload limits (max 3 per ticket).</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public TicketService(TicketRepository ticketRepository,
                         TicketCommentRepository commentRepository,
                         FileStorageService fileStorageService,
                         UserRepository userRepository,
                         EmailService emailService,
                         AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ticket CRUD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new incident ticket with status {@link TicketStatus#OPEN}.
     *
     * @param request     validated request body
     * @param currentUser the authenticated user submitting the ticket
     * @return the persisted ticket as a {@link TicketResponse}
     */
    public TicketResponse createTicket(CreateTicketRequest request, User currentUser) {
        IncidentTicket ticket = new IncidentTicket();
        ticket.setCreatedBy(currentUser);
        ticket.setLocation(request.getLocation());
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setPreferredContact(request.getPreferredContact());
        ticket.setResourceId(request.getResourceId());
        ticket.setStatus(TicketStatus.OPEN);

        IncidentTicket saved = ticketRepository.save(ticket);
        log.info("Ticket #{} created by user '{}'", saved.getId(), currentUser.getEmail());

        // Notify the user who created the ticket
        emailService.notifyTicketCreated(saved);

        // Notify all admins and managers
        List<User> adminsAndManagers = userRepository.findByRoleIn(List.of(Role.ADMIN, Role.MANAGER));
        emailService.notifyAdminsAndManagersNewTicket(saved, adminsAndManagers);

        // Audit
        auditLogService.log(AuditAction.TICKET_CREATED, currentUser,
                "Ticket #" + saved.getId() + " (" + saved.getCategory() + ") created: " + saved.getLocation(),
                "Ticket", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Updates an existing ticket's details.
     * Allowed only for the creator (while OPEN) or an ADMIN.
     *
     * @param id          target ticket
     * @param request     new details
     * @param currentUser authenticated caller
     * @return updated ticket
     */
    public TicketResponse updateTicket(@NonNull Long id, UpdateTicketRequest request, User currentUser) {
        IncidentTicket ticket = findTicketOrThrow(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isCreator = ticket.getCreatedBy() != null && Objects.equals(ticket.getCreatedBy().getId(), currentUser.getId());
        boolean isOpen = ticket.getStatus() == TicketStatus.OPEN;

        if (!isAdmin && !(isCreator && isOpen)) {
            throw new AccessDeniedException("You are not authorised to edit this ticket. " +
                    (isCreator ? "Tickets can only be edited while they are OPEN." : "Only the admin or creator may edit tickets."));
        }

        ticket.setLocation(request.getLocation());
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setPreferredContact(request.getPreferredContact());
        ticket.setResourceId(request.getResourceId());

        IncidentTicket updated = ticketRepository.save(ticket);
        log.info("Ticket #{} updated by user '{}'", id, currentUser.getEmail());
        auditLogService.log(AuditAction.TICKET_UPDATED, currentUser,
                "Ticket #" + id + " updated: " + updated.getCategory() + " at " + updated.getLocation(),
                "Ticket", id);
        return mapToResponse(updated);
    }

    /**
     * Retrieves a single ticket by its ID.
     *
     * @param id ticket primary key
     * @return the ticket as a {@link TicketResponse}
     * @throws ResourceNotFoundException if no ticket exists with the given id
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(@NonNull Long id) {
        IncidentTicket ticket = findTicketOrThrow(id);
        return mapToResponse(ticket);
    }

    /**
     * Returns tickets visible to the current user with pagination and database-level filtering.
     *
     * @param statusFilter   optional status string to filter by
     * @param categoryFilter optional category string to filter by
     * @param priorityFilter optional priority string to filter by
     * @param page           page number (0-indexed)
     * @param size           page size
     * @param currentUser    the authenticated caller
     * @return a page of {@link TicketResponse}
     */
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(String statusFilter, String categoryFilter, String priorityFilter, String keyword, int page, int size, User currentUser) {
        boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.MANAGER;
        boolean isTechnician = currentUser.getRole() == Role.TECHNICIAN;

        TicketStatus status = (statusFilter != null && !statusFilter.isBlank()) ? parseStatus(statusFilter) : null;
        User creator = (!isAdminOrManager && !isTechnician) ? currentUser : null;
        User technician = isTechnician ? currentUser : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<IncidentTicket> ticketPage = ticketRepository.findAllWithFilters(
                status, categoryFilter, priorityFilter, creator, technician, keyword, pageable
        );

        return ticketPage.map(this::mapToResponse);
    }

    /**
     * Updates the status of a ticket, enforcing strict transition rules.
     *
     * <p>Valid transitions:
     * <pre>
     *   OPEN        → IN_PROGRESS | REJECTED
     *   IN_PROGRESS → RESOLVED    | REJECTED
     *   RESOLVED    → CLOSED      | REJECTED
     *   CLOSED      → (terminal)
     *   REJECTED    → (terminal)
     * </pre>
     *
     * @param id          ticket ID
     * @param request     the requested new status and optional notes
     * @param currentUser must be ADMIN or TECHNICIAN
     * @return updated ticket as {@link TicketResponse}
     * @throws IllegalStateException if the transition is not permitted
     */
    public TicketResponse updateTicketStatus(@NonNull Long id,
                                             UpdateTicketStatusRequest request,
                                             User currentUser) {
        if (currentUser.getRole() == Role.USER) {
            throw new AccessDeniedException("Only ADMIN or TECHNICIAN can update ticket status.");
        }

        IncidentTicket ticket = findTicketOrThrow(id);
        TicketStatus current = ticket.getStatus();
        TicketStatus next = request.getStatus();

        validateTransition(current, next);

        ticket.setStatus(next);
        if (request.getResolutionNotes() != null) {
            ticket.setResolutionNotes(request.getResolutionNotes());
        }
        IncidentTicket updated = ticketRepository.save(ticket);
        log.info("Ticket #{} status changed: {} → {} by '{}'",
                id, current, next, currentUser.getEmail());

        // Notify the ticket creator about the status change
        emailService.notifyStatusChange(updated, current);

        // Audit
        auditLogService.log(AuditAction.TICKET_STATUS_UPDATED, currentUser,
                "Ticket #" + id + " status: " + current + " → " + next,
                "Ticket", id);

        return mapToResponse(updated);
    }

    /**
     * Assigns a technician to a ticket and sets its status to
     * {@link TicketStatus#IN_PROGRESS}.
     * Only ADMIN users may call this method (enforced by the controller).
     *
     * @param ticketId     the ticket to assign
     * @param technicianId ID of the user with TECHNICIAN role
     * @param currentUser  must be ADMIN
     * @return updated ticket as {@link TicketResponse}
     * @throws ResourceNotFoundException if the technician user does not exist
     */
    public TicketResponse assignTechnician(@NonNull Long ticketId, @NonNull Long technicianId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can assign technicians.");
        }

        IncidentTicket ticket = findTicketOrThrow(ticketId);
        User technician = userRepository.findById(Objects.requireNonNull(technicianId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Technician not found with id: " + technicianId));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new IllegalArgumentException("User with id " + technicianId + " is not a Technician.");
        }

        ticket.setAssignedTechnician(technician);
        // Automatically move ticket to IN_PROGRESS when assigned
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        IncidentTicket updated = ticketRepository.save(ticket);
        log.info("Ticket #{} assigned to technician '{}' by admin '{}'",
                ticketId, technician.getEmail(), currentUser.getEmail());

        // Notify technician via email + in-app push (both handled inside notifyTechnicianAssigned)
        emailService.notifyTechnicianAssigned(updated, technician);

        // Audit
        auditLogService.log(AuditAction.TICKET_ASSIGNED, currentUser,
                "Ticket #" + ticketId + " assigned to technician '" + technician.getName() + "'",
                "Ticket", ticketId);

        return mapToResponse(updated);
    }

    /**
     * Uploads up to 3 images for a ticket, enforcing the cumulative 3-image cap.
     *
     * @param ticketId the ticket to attach images to
     * @param files    list of image files (must not exceed total of 3 with existing)
     * @return updated ticket as {@link TicketResponse}
     * @throws IllegalStateException if the upload would exceed the 3-image limit
     */
    public TicketResponse uploadImages(@NonNull Long ticketId, List<MultipartFile> files) {
        IncidentTicket ticket = findTicketOrThrow(ticketId);

        int existing = ticket.getImageUrls().size();
        if (existing + files.size() > 3) {
            throw new IllegalStateException(
                    "A ticket may have at most 3 images. Currently has " + existing
                            + "; tried to add " + files.size() + ".");
        }

        for (MultipartFile file : files) {
            String path = fileStorageService.store(file);
            ticket.getImageUrls().add(path);
        }

        IncidentTicket updated = ticketRepository.save(ticket);
        log.info("Added {} image(s) to ticket #{}", files.size(), ticketId);
        return mapToResponse(updated);
    }

    /**
     * Submits a rating and optional feedback for a resolved or closed ticket.
     * Only the ticket creator is allowed to provide feedback.
     *
     * @param id          ticket to rate
     * @param request     rating and feedback text
     * @param currentUser authenticated caller
     * @return updated ticket
     */
    public TicketResponse submitFeedback(@NonNull Long id, SubmitFeedbackRequest request, User currentUser) {
        IncidentTicket ticket = findTicketOrThrow(id);

        boolean isCreator = ticket.getCreatedBy() != null && Objects.equals(ticket.getCreatedBy().getId(), currentUser.getId());
        if (!isCreator) {
            throw new AccessDeniedException("Only the person who submitted the ticket can provide feedback.");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new IllegalStateException("Feedback can only be provided for RESOLVED or CLOSED tickets.");
        }

        ticket.setRating(request.getRating());
        ticket.setUserFeedback(request.getUserFeedback());

        IncidentTicket updated = ticketRepository.save(ticket);
        log.info("Feedback received for ticket #{} from user '{}'", id, currentUser.getEmail());
        auditLogService.log(AuditAction.TICKET_FEEDBACK_SUBMITTED, currentUser,
                "Feedback submitted for Ticket #" + id + " with rating " + request.getRating(),
                "Ticket", id);
        return mapToResponse(updated);
    }

    /**
     * Hard-deletes a ticket. All roles can delete tickets with the following rules:
     * <ul>
     *   <li>ADMIN / MANAGER — can delete any ticket regardless of status.</li>
     *   <li>TECHNICIAN — can delete tickets assigned to them.</li>
     *   <li>USER (creator) — can delete their own tickets.</li>
     * </ul>
     *
     * @param id          ticket to delete
     * @param currentUser the authenticated user requesting deletion
     */
    public void deleteTicket(@NonNull Long id, User currentUser) {
        IncidentTicket ticket = findTicketOrThrow(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isManager = currentUser.getRole() == Role.MANAGER;
        boolean isTechnician = currentUser.getRole() == Role.TECHNICIAN;
        boolean isCreator = ticket.getCreatedBy() != null && Objects.equals(ticket.getCreatedBy().getId(), currentUser.getId());
        boolean isAssignedTechnician = isTechnician && ticket.getAssignedTechnician() != null
                && Objects.equals(ticket.getAssignedTechnician().getId(), currentUser.getId());

        // ADMIN and MANAGER can always delete any ticket
        // TECHNICIAN can delete tickets assigned to them
        // Creator (USER) can delete their own tickets
        if (!isAdmin && !isManager && !isAssignedTechnician && !isCreator) {
            throw new AccessDeniedException("You are not authorized to delete this ticket.");
        }

        ticketRepository.delete(ticket);
        log.info("Ticket #{} deleted by user '{}' (role: {})", id, currentUser.getEmail(), currentUser.getRole());

        // Audit
        auditLogService.log(AuditAction.TICKET_DELETED, currentUser,
                "Ticket #" + id + " permanently deleted by " + currentUser.getRole(),
                "Ticket", id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Comment Operations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Adds a comment to a ticket.
     *
     * @param ticketId    the target ticket
     * @param request     validated comment body
     * @param currentUser the authenticated author
     * @return the persisted comment as {@link CommentResponse}
     */
    public CommentResponse addComment(@NonNull Long ticketId, CommentRequest request, User currentUser) {
        IncidentTicket ticket = findTicketOrThrow(ticketId);

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(currentUser);
        comment.setContent(request.getContent());

        TicketComment saved = commentRepository.save(comment);
        log.info("Comment #{} added to ticket #{} by '{}'",
                saved.getId(), ticketId, currentUser.getEmail());

        // Notify the ticket creator when an admin, manager, or technician adds a comment
        boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.MANAGER
                || currentUser.getRole() == Role.TECHNICIAN;
        if (isAdminOrManager && ticket.getCreatedBy() != null
                && !ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            emailService.notifyCommentAdded(ticket, saved, currentUser);
        }

        // Audit
        auditLogService.log(AuditAction.TICKET_COMMENT_ADDED, currentUser,
                "Comment added to Ticket #" + ticketId + " by " + currentUser.getName(),
                "Ticket", ticketId);

        return mapToCommentResponse(saved);
    }

    /**
     * Edits an existing comment. Only the original author or an ADMIN may edit.
     *
     * @param commentId   the comment to edit
     * @param request     new content
     * @param currentUser the authenticated caller
     * @return updated comment as {@link CommentResponse}
     * @throws AccessDeniedException if the caller is not the author or ADMIN
     */
    public CommentResponse editComment(@NonNull Long commentId, CommentRequest request, User currentUser) {
        TicketComment comment = findCommentOrThrow(commentId);
        assertOwnerOrAdmin(comment.getAuthor(), currentUser, "edit");

        comment.setContent(request.getContent());
        TicketComment updated = commentRepository.save(comment);
        log.info("Comment #{} edited by '{}'", commentId, currentUser.getEmail());
        auditLogService.log(AuditAction.TICKET_COMMENT_EDITED, currentUser,
                "Comment #" + commentId + " edited on Ticket #" + comment.getTicket().getId(),
                "Ticket", comment.getTicket().getId());
        return mapToCommentResponse(updated);
    }

    /**
     * Deletes a comment. Only the original author or an ADMIN may delete.
     *
     * @param commentId   the comment to delete
     * @param currentUser the authenticated caller
     * @throws AccessDeniedException if the caller is not the author or ADMIN
     */
    public void deleteComment(@NonNull Long commentId, User currentUser) {
        TicketComment comment = findCommentOrThrow(commentId);
        assertOwnerOrAdmin(comment.getAuthor(), currentUser, "delete");
        Long ticketId = comment.getTicket().getId();
        commentRepository.delete(comment);
        log.info("Comment #{} deleted by '{}'", commentId, currentUser.getEmail());
        auditLogService.log(AuditAction.TICKET_COMMENT_DELETED, currentUser,
                "Comment #" + commentId + " deleted from Ticket #" + ticketId,
                "Ticket", ticketId);
    }

    /**
     * Returns all comments for a ticket ordered by creation time.
     *
     * @param ticketId the ticket whose comments to fetch
     * @return list of {@link CommentResponse}
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CommentResponse> getComments(@org.springframework.lang.NonNull Long ticketId, int page, int size) {
        IncidentTicket ticket = findTicketOrThrow(ticketId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return commentRepository.findByTicket(ticket, pageable)
                .map(this::mapToCommentResponse);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private IncidentTicket findTicketOrThrow(@NonNull Long id) {
        return ticketRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private TicketComment findCommentOrThrow(@NonNull Long id) {
        return commentRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    private void assertOwnerOrAdmin(User author, User currentUser, String action) {
        boolean isOwner = author != null && Objects.equals(author.getId(), currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not authorised to " + action + " this comment.");
        }
    }

    /**
     * Validates a requested status transition and throws {@link IllegalStateException}
     * with a descriptive message if the transition is not allowed.
     */
    private void validateTransition(TicketStatus current, TicketStatus next) {
        boolean valid = switch (current) {
            case OPEN        -> next == TicketStatus.IN_PROGRESS || next == TicketStatus.REJECTED;
            case IN_PROGRESS -> next == TicketStatus.RESOLVED    || next == TicketStatus.REJECTED;
            case RESOLVED    -> next == TicketStatus.CLOSED      || next == TicketStatus.REJECTED;
            case CLOSED, REJECTED -> false; // terminal states
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid status transition: cannot move from " + current + " to " + next + ". "
                            + "Allowed from " + current + ": " + allowedFrom(current));
        }
    }

    private String allowedFrom(TicketStatus status) {
        return switch (status) {
            case OPEN        -> "[IN_PROGRESS, REJECTED]";
            case IN_PROGRESS -> "[RESOLVED, REJECTED]";
            case RESOLVED    -> "[CLOSED, REJECTED]";
            case CLOSED, REJECTED -> "none (terminal state)";
        };
    }

    private TicketStatus parseStatus(String raw) {
        try {
            return TicketStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown ticket status: " + raw);
        }
    }

    /**
     * Maps an {@link IncidentTicket} entity to a {@link TicketResponse} DTO.
     */
    private TicketResponse mapToResponse(IncidentTicket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setLocation(ticket.getLocation());
        response.setCategory(ticket.getCategory());
        response.setDescription(ticket.getDescription());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setImageUrls(ticket.getImageUrls());
        response.setResolutionNotes(ticket.getResolutionNotes());
        response.setPreferredContact(ticket.getPreferredContact());
        response.setResourceId(ticket.getResourceId());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setRating(ticket.getRating());
        response.setUserFeedback(ticket.getUserFeedback());

        if (ticket.getCreatedBy() != null) {
            response.setCreatedByName(ticket.getCreatedBy().getName());
            response.setCreatedByEmail(ticket.getCreatedBy().getEmail());
            response.setCreatedById(ticket.getCreatedBy().getId());
        }

        if (ticket.getAssignedTechnician() != null) {
            response.setAssignedTechnicianName(ticket.getAssignedTechnician().getName());
            response.setAssignedTechnicianId(ticket.getAssignedTechnician().getId());
        }

        return response;
    }

    /**
     * Maps a {@link TicketComment} entity to a {@link CommentResponse} DTO.
     */
    private CommentResponse mapToCommentResponse(TicketComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        if (comment.getAuthor() != null) {
            response.setAuthorName(comment.getAuthor().getName());
            response.setAuthorId(comment.getAuthor().getId());
        }
        return response;
    }
}
