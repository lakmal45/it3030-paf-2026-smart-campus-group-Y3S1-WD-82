package com.project.paf.modules.booking.service;

import com.project.paf.modules.auditlog.AuditAction;
import com.project.paf.modules.auditlog.AuditLogService;
import com.project.paf.modules.booking.entity.Booking;
import com.project.paf.modules.booking.entity.BookingStatus;
import com.project.paf.modules.booking.repository.BookingRepository;
import com.project.paf.modules.notification.service.EmailService;
import com.project.paf.modules.user.model.Role;
import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.repository.UserRepository;
import com.project.paf.modules.resource.model.ResourceStatus;
import com.project.paf.modules.resource.repository.ResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final AuditLogService auditLogService;

    public BookingService(BookingRepository bookingRepository,
                          EmailService emailService,
                          UserRepository userRepository,
                          ResourceRepository resourceRepository,
                          AuditLogService auditLogService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Create a new booking for the given user.
     * Checks for time-slot conflicts before saving.
     * Sends email + in-app notification to the user and all admins/managers.
     */
    public Booking createBooking(String resource, LocalDate date, LocalTime startTime,
                                  LocalTime endTime, String reason, User user) {

        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        // Strict Enforcement: Resource must be in ACTIVE status to be bookable
        resourceRepository.findByNameIgnoreCase(resource).ifPresent(res -> {
            if (res.getStatus() != ResourceStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Selection Unavailable: The resource '" + resource + "' is currently " +
                    res.getStatus().name().replace('_', ' ') + " and cannot be reserved at this time.");
            }
        });

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                resource, date, startTime, endTime, BookingStatus.CANCELLED);

        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Time conflict with an existing confirmed booking.");
        }

        Booking booking = new Booking();
        booking.setResource(resource);
        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setReason(reason);
        booking.setStatus(BookingStatus.PENDING);
        booking.setUser(user);
        Booking saved = bookingRepository.save(booking);

        // Notify user (async: email + in-app)
        emailService.notifyBookingCreatedToUser(saved);

        // Notify all admins and managers (async: email + in-app)
        List<User> adminsAndManagers = userRepository.findByRoleIn(
                Arrays.asList(Role.ADMIN, Role.MANAGER));
        emailService.notifyBookingCreatedToAdmins(saved, adminsAndManagers);

        // Audit
        auditLogService.log(AuditAction.BOOKING_CREATED, user,
                "Booking #" + saved.getId() + " for '" + saved.getResource() + "' on " + saved.getDate(),
                "Booking", saved.getId());

        return saved;
    }

    /**
     * Get all bookings belonging to a specific user.
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all bookings (Admin/Manager overview).
     */
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllWithUser();
    }

    /**
     * Update an existing booking (Admin/Manager control).
     */
    public Booking updateBooking(Long id, String resource, LocalDate date, LocalTime startTime, LocalTime endTime, String statusStr) {
        Booking booking = bookingRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        // Check for conflicts excluding this specific booking
        List<Booking> conflicts = bookingRepository.findConflictingBookings(resource, date, startTime, endTime, BookingStatus.CANCELLED);
        boolean realConflict = conflicts.stream().anyMatch(b -> !b.getId().equals(id));

        if (realConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot update: Conflicting reservation exists.");
        }

        booking.setResource(resource);
        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        BookingStatus previousStatus = booking.getStatus();
        if (statusStr != null) {
            try {
                booking.setStatus(BookingStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking status.");
            }
        }

        Booking saved = bookingRepository.save(booking);

        // Notify the booking owner when an admin cancels their booking
        if (saved.getStatus() == BookingStatus.CANCELLED
                && previousStatus != BookingStatus.CANCELLED) {
            emailService.notifyBookingCancelled(saved, "an administrator");
        }

        // Audit - determine the action from the status change
        AuditAction updateAction = (saved.getStatus() == BookingStatus.CANCELLED
                && previousStatus != BookingStatus.CANCELLED)
                ? AuditAction.BOOKING_CANCELLED
                : AuditAction.BOOKING_UPDATED;
        auditLogService.log(updateAction, null,
                "Booking #" + saved.getId() + " for '" + saved.getResource() + "' -> " + saved.getStatus(),
                "Booking", saved.getId());

        return saved;
    }

    /**
     * Delete a booking records permanently.
     */
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(java.util.Objects.requireNonNull(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        bookingRepository.deleteById(java.util.Objects.requireNonNull(id));
        auditLogService.log(AuditAction.BOOKING_DELETED, null,
                "Booking #" + id + " permanently deleted",
                "Booking", id);
    }

    /**
     * Confirm a booking (Admin/Manager control).
     * Sends email + in-app notification to the booking owner.
     */
    public Booking confirmBooking(Long id) {
        Booking booking = bookingRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        // Notify the booking owner (async: email + in-app)
        emailService.notifyBookingConfirmed(saved);

        // Audit
        auditLogService.log(AuditAction.BOOKING_CONFIRMED, null,
                "Booking #" + saved.getId() + " for '" + saved.getResource() + "' confirmed",
                "Booking", saved.getId());

        return saved;
    }

    /**
     * Cancel a booking by the owning user.
     * Sends email + in-app notification to the booking owner.
     */
    public Booking cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findByIdWithUser(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Unauthorized cancellation attempt.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Notify the booking owner (async: email + in-app)
        emailService.notifyBookingCancelled(saved, "you");

        // Audit
        auditLogService.log(AuditAction.BOOKING_CANCELLED, user,
                "Booking #" + saved.getId() + " for '" + saved.getResource() + "' cancelled by user",
                "Booking", saved.getId());

        return saved;
    }

    /**
     * Reject a booking (Admin/Manager control).
     * Sends email + in-app notification to the booking owner.
     */
    public Booking rejectBooking(Long id) {
        Booking booking = bookingRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Notify the booking owner of rejection (async: email + in-app)
        emailService.notifyBookingCancelled(saved, "an administrator");

        // Audit
        auditLogService.log(AuditAction.BOOKING_REJECTED, null,
                "Booking #" + saved.getId() + " for '" + saved.getResource() + "' rejected by admin",
                "Booking", saved.getId());

        return saved;
    }
}
