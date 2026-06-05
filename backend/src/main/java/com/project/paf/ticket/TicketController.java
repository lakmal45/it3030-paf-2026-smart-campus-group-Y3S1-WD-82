package com.project.paf.ticket;
import java.util.List;
import org.springframework.lang.NonNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.project.paf.modules.user.model.Role;
import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for the Maintenance & Incident Ticketing System.
 *
 * <p>Base path: {@code /api/tickets}
 *
 * <p>Authentication is enforced at the SecurityConfig level.
 * Fine-grained role checks use {@code @PreAuthorize} or are delegated to the service.
 */
@Slf4j
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ticket Endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/tickets — Create a new incident ticket.
     * Accessible to all authenticated users.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        TicketResponse response = ticketService.createTicket(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/tickets — List tickets (filtered by role and optional status).
     * ADMIN/TECHNICIAN: all tickets. USER: own tickets only.
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<TicketResponse>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        return ResponseEntity.ok(ticketService.getAllTickets(status, category, priority, keyword, page, size, currentUser));
    }

    /**
     * GET /api/tickets/{id} — Get a single ticket by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    /**
     * PUT /api/tickets/{id} — Update ticket details (location, description, etc.).
     * Only the creator (if OPEN) or an ADMIN can call this.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UpdateTicketRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        return ResponseEntity.ok(ticketService.updateTicket(id, request, currentUser));
    }

    /**
     * PUT /api/tickets/{id}/status — Update ticket status.
     * Only ADMIN or TECHNICIAN may call this endpoint.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        requireRoles(currentUser, Role.ADMIN, Role.TECHNICIAN, Role.MANAGER);
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request, currentUser));
    }

    /**
     * PUT /api/tickets/{id}/assign — Assign a ticket to a technician.
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody AssignTechnicianRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        requireRoles(currentUser, Role.ADMIN);
        return ResponseEntity.ok(
                ticketService.assignTechnician(id, java.util.Objects.requireNonNull(request.getTechnicianId()), currentUser));
    }

    /**
     * PUT /api/tickets/{id}/feedback — Submit user rating and feedback.
     * Only the ticket creator may call this endpoint for RESOLVED/CLOSED tickets.
     */
    @PutMapping("/{id}/feedback")
    public ResponseEntity<TicketResponse> submitFeedback(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody SubmitFeedbackRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        return ResponseEntity.ok(ticketService.submitFeedback(id, request, currentUser));
    }

    /**
     * DELETE /api/tickets/{id} — Hard-delete a ticket.
     * Authorization is enforced in the service layer (all roles can delete with rules).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTicket(
            @PathVariable @NonNull Long id, 
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        User currentUser = resolveUser(session, emailHeader);
        ticketService.deleteTicket(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/tickets/{id}/images — Upload images (max 3 total per ticket).
     * Accepts multipart/form-data with field name "files".
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketResponse> uploadImages(
            @PathVariable @NonNull Long id,
            @RequestParam("files") List<MultipartFile> files) {

        return ResponseEntity.ok(ticketService.uploadImages(id, files));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Comment Endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/tickets/{id}/comments — List all comments for a ticket.
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<org.springframework.data.domain.Page<CommentResponse>> getComments(
            @PathVariable @org.springframework.lang.NonNull Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getComments(id, page, size));
    }

    /**
     * POST /api/tickets/{id}/comments — Add a comment.
     * Returns 201 Created.
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody CommentRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        CommentResponse response = ticketService.addComment(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/tickets/{ticketId}/comments/{commentId} — Edit a comment.
     * Ownership is enforced in the service layer.
     */
    @PutMapping("/{ticketId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable @NonNull Long ticketId,
            @PathVariable @NonNull Long commentId,
            @Valid @RequestBody CommentRequest request,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        return ResponseEntity.ok(ticketService.editComment(commentId, request, currentUser));
    }

    /**
     * DELETE /api/tickets/{ticketId}/comments/{commentId} — Delete a comment.
     * Ownership is enforced in the service layer.
     */
    @DeleteMapping("/{ticketId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteComment(
            @PathVariable @NonNull Long ticketId,
            @PathVariable @NonNull Long commentId,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {

        User currentUser = resolveUser(session, emailHeader);
        ticketService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resolves the current {@link User} from either the session or the X-User-Email header.
     * Throws 401 Unauthorized if no user can be identified.
     */
    private User resolveUser(HttpSession session, String emailHeader) {
        User resolved = null;

        // 1. Try session first
        Object sessionAttr = session.getAttribute("user");
        if (sessionAttr instanceof User) {
            resolved = (User) sessionAttr;
        }

        // 2. Fall back to email header
        if (resolved == null && emailHeader != null && !emailHeader.isBlank()) {
            resolved = userRepository.findByEmail(emailHeader).orElse(null);
        }

        if (resolved == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return resolved;
    }

    /**
     * Guard: verifies the resolved user has any of the required roles.
     * Throws 403 Forbidden if the user lacks the required role.
     */
    private void requireRoles(User user, Role... allowedRoles) {
        boolean hasRole = false;
        for (Role role : allowedRoles) {
            if (user.getRole() == role) {
                hasRole = true;
                break;
            }
        }
        if (!hasRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized: Role not matching.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<java.util.Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("error", "Validation Failed");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<java.util.Map<String, String>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("error", "Validation Failed");
        body.put("message", ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b).orElse("Invalid request"));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<java.util.Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("Response Status Error: {}", ex.getMessage());
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("error", ex.getReason() != null ? ex.getReason() : "Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<java.util.Map<String, String>> handleAllExceptions(Exception ex) {
        log.error("Internal Server Error: ", ex);
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
