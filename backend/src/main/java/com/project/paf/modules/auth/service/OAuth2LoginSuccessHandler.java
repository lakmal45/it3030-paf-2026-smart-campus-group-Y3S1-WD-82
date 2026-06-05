package com.project.paf.modules.auth.service;

import com.project.paf.modules.auditlog.AuditAction;
import com.project.paf.modules.auditlog.AuditLogService;
import com.project.paf.modules.notification.service.EmailService;
import com.project.paf.modules.user.model.Role;
import com.project.paf.modules.user.model.User;
import com.project.paf.modules.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, EmailService emailService,
                                     AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String profileImageUrl = oAuth2User.getAttribute("picture");

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        // Create user if first-time Google login
        boolean isNewUser = existingUser.isEmpty();
        if (isNewUser) {

            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(null); // Google users have no local password

            // default role
            user.setRole(Role.USER);
        } else {
            user = existingUser.get();
        }

        user.setName(name);
        user.setProfileImageUrl(profileImageUrl);
        user = userRepository.save(user);
        request.getSession().setAttribute("user", user);

        // Send welcome email only on first-time signup (async, best-effort)
        if (isNewUser) {
            emailService.notifyWelcome(name, email, true);
            auditLogService.log(AuditAction.USER_SIGNED_UP, user,
                    "New user '" + user.getName() + "' (" + user.getEmail() + ") signed up with Google",
                    "User", user.getId());
        }

        // Get role from database
        String role = user.getRole().name();

        response.sendRedirect(
                "http://localhost:5173/oauth-success?email="
                        + encodeParam(email) +
                        "&name="
                        + encodeParam(name)
                        + "&role=" + encodeParam(role)
                        + "&profileImageUrl=" + encodeParam(profileImageUrl)
        );
    }

    private String encodeParam(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
