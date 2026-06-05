package com.project.paf.modules.notification.controller;

import com.project.paf.modules.notification.dto.NotificationResponse;
import com.project.paf.modules.notification.service.AppNotificationService;
import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final AppNotificationService appNotificationService;
    private final UserRepository userRepository;

    public NotificationController(AppNotificationService appNotificationService, UserRepository userRepository) {
        this.appNotificationService = appNotificationService;
        this.userRepository = userRepository;
    }

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

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        User user = resolveUser(session, emailHeader);
        return ResponseEntity.ok(appNotificationService.getUserNotifications(user));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        User user = resolveUser(session, emailHeader);
        return ResponseEntity.ok(appNotificationService.getUnreadCount(user));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        User user = resolveUser(session, emailHeader);
        appNotificationService.markAsRead(java.util.Objects.requireNonNull(id), user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        User user = resolveUser(session, emailHeader);
        appNotificationService.markAllAsRead(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            HttpSession session,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        User user = resolveUser(session, emailHeader);
        appNotificationService.deleteNotification(java.util.Objects.requireNonNull(id), user);
        return ResponseEntity.noContent().build();
    }
}
