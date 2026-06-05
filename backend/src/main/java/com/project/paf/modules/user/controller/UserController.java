package com.project.paf.modules.user.controller;

import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody User updateData, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        User updatedUser = userService.updateUser(java.util.Objects.requireNonNull(currentUser.getId()), updateData);
        session.setAttribute("user", updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        try {
            userService.changePassword(java.util.Objects.requireNonNull(currentUser.getId()), oldPassword, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me/notifications")
    public ResponseEntity<?> updateNotificationPrefs(@RequestBody Map<String, Object> request, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        // Pass null for any key not in the request — the service will keep the existing DB value
        Boolean emailEnabled = request.containsKey("emailNotificationsEnabled")
                ? Boolean.TRUE.equals(request.get("emailNotificationsEnabled"))
                : null;
        Boolean pushEnabled = request.containsKey("pushNotificationsEnabled")
                ? Boolean.TRUE.equals(request.get("pushNotificationsEnabled"))
                : null;
        User updatedUser = userService.updateNotificationPrefs(java.util.Objects.requireNonNull(currentUser.getId()), emailEnabled, pushEnabled);
        session.setAttribute("user", updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        userService.deleteUser(java.util.Objects.requireNonNull(currentUser.getId()), currentUser);
        session.invalidate();
        return ResponseEntity.ok("Account deleted successfully");
    }
}
