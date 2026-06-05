package com.project.paf.modules.auditlog;

import com.project.paf.modules.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central service for writing and querying audit log entries.
 *
 * <p>All write operations use a lightweight, non-blocking call pattern:
 * the {@link #log} method is intentionally kept side-effect-free from the
 * caller's perspective — a failure to write a log entry never propagates
 * an exception back to the business operation that triggered it.
 *
 * <p>A scheduled job purges entries older than 30 days.
 */
@Slf4j
@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Records a single audit event.
     *
     * @param action            the action that occurred
     * @param actor             the user who performed the action (may be null for system events)
     * @param targetDescription human-readable summary, e.g. "Booking #12 for Conference Room A"
     * @param entityType        e.g. "Booking", "Ticket", "User", "Resource"
     * @param entityId          primary key of the affected entity
     */
    @Transactional
    public void log(AuditAction action,
                    User actor,
                    String targetDescription,
                    String entityType,
                    Long entityId) {
        String actorName = actor != null ? actor.getName() : null;
        String actorRole = actor != null && actor.getRole() != null ? actor.getRole().name() : null;
        log(action, actor, actorName, actorRole, targetDescription, entityType, entityId);
    }

    @Transactional
    public void log(AuditAction action,
                    User actor,
                    String actorName,
                    String actorRole,
                    String targetDescription,
                    String entityType,
                    Long entityId) {
        try {
            AuditLog entry = new AuditLog();
            entry.setAction(action);
            entry.setCategory(action.getCategory());
            entry.setActor(actor);
            entry.setActorName(actorName != null && !actorName.isBlank() ? actorName : "System");
            entry.setActorRole(actorRole != null && !actorRole.isBlank() ? actorRole : "SYSTEM");
            entry.setTargetDescription(targetDescription);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            repository.save(entry);
        } catch (Exception ex) {
            // Log the error but never let audit failures break business operations
            log.error("Failed to write audit log entry for action {}: {}", action, ex.getMessage());
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns a paginated, filtered page of audit log entries for the admin UI.
     *
     * @param category optional category filter (BOOKING | TICKET | USER | RESOURCE)
     * @param search   optional free-text search across actor name and description
     * @param from     optional start date (inclusive)
     * @param to       optional end date (inclusive)
     * @param page     zero-based page index
     * @param size     page size
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getLogs(String category,
                                     String search,
                                     LocalDate from,
                                     LocalDate to,
                                     int page,
                                     int size) {

        String catFilter    = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL"))
                ? category.toUpperCase() : null;
        String searchFilter = (search != null && !search.isBlank()) ? search : null;
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to   != null) ? to.atTime(23, 59, 59) : null;

        Pageable pageable = PageRequest.of(page, size);
        return repository.findFiltered(catFilter, searchFilter, fromDt, toDt, pageable)
                .map(this::toDTO);
    }

    /**
     * Returns per-category event counts for the summary cards, plus a total.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        long total    = repository.count();
        long bookings = repository.countByCategory("BOOKING");
        long tickets  = repository.countByCategory("TICKET");
        long users    = repository.countByCategory("USER");
        long resources = repository.countByCategory("RESOURCE");

        stats.put("total",     total);
        stats.put("bookings",  bookings);
        stats.put("tickets",   tickets);
        stats.put("users",     users);
        stats.put("resources", resources);
        return stats;
    }

    // ── Scheduled Purge ───────────────────────────────────────────────────────

    /**
     * Runs every day at 02:00 AM and removes audit log entries older than 30 days.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = repository.deleteOlderThan(cutoff);
        log.info("Audit log purge: removed {} entries older than 30 days.", deleted);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private AuditLogDTO toDTO(AuditLog entry) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(entry.getId());
        dto.setAction(entry.getAction().name());
        dto.setActionLabel(entry.getAction().getLabel());
        dto.setCategory(entry.getCategory());
        dto.setActorName(entry.getActorName());
        dto.setActorRole(entry.getActorRole());
        dto.setTargetDescription(entry.getTargetDescription());
        dto.setEntityType(entry.getEntityType());
        dto.setEntityId(entry.getEntityId());
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }
}
