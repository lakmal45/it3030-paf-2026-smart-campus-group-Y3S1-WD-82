package com.project.paf.modules.notification.model;

import com.project.paf.ticket.IncidentTicket;
import com.project.paf.modules.user.model.User;

/**
 * Utility class that produces HTML email bodies for each notification type.
 *
 * <p>All methods are static — this class is not instantiated by Spring;
 * it exists purely as a template factory for the {@link com.project.paf.modules.notification.service.EmailService}.
 */
public final class EmailTemplates {

    private EmailTemplates() {}

    // ─────────────────────────────────────────────────────────────────────────
    // Shared styles
    // ─────────────────────────────────────────────────────────────────────────

    private static final String STYLE = """
            <style>
              body { font-family: 'Segoe UI', Arial, sans-serif; background: #f4f6f9; margin: 0; padding: 0; }
              .wrapper { max-width: 600px; margin: 32px auto; background: #ffffff;
                         border-radius: 12px; overflow: hidden;
                         box-shadow: 0 4px 20px rgba(0,0,0,.08); }
              .header { background: linear-gradient(135deg, #1e3a5f 0%, #2d6a9f 100%);
                        padding: 28px 36px; }
              .header h1 { margin: 0; color: #ffffff; font-size: 22px; font-weight: 700; }
              .header p  { margin: 4px 0 0; color: #a8c8e8; font-size: 13px; }
              .body { padding: 32px 36px; }
              .body p { color: #374151; font-size: 15px; line-height: 1.6; margin: 0 0 16px; }
              .ticket-card { background: #f0f7ff; border-left: 4px solid #2d6a9f;
                             border-radius: 8px; padding: 16px 20px; margin: 20px 0; }
              .ticket-card table { width: 100%; border-collapse: collapse; }
              .ticket-card td { padding: 6px 0; font-size: 14px; color: #374151; vertical-align: top; }
              .ticket-card td:first-child { font-weight: 600; width: 140px; color: #1e3a5f; }
              .badge { display: inline-block; padding: 3px 10px; border-radius: 999px;
                       font-size: 12px; font-weight: 600; }
              .badge-open       { background: #dbeafe; color: #1d4ed8; }
              .badge-progress   { background: #fef3c7; color: #92400e; }
              .badge-resolved   { background: #d1fae5; color: #065f46; }
              .badge-closed     { background: #e5e7eb; color: #374151; }
              .badge-rejected   { background: #fee2e2; color: #991b1b; }
              .footer { background: #f8fafc; padding: 18px 36px;
                        border-top: 1px solid #e5e7eb; text-align: center; }
              .footer p { margin: 0; font-size: 12px; color: #9ca3af; }
            </style>
            """;

    // ─────────────────────────────────────────────────────────────────────────
    // Welcome / Account Created
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML welcome email sent when a new account is created.
     *
     * @param name        the user's display name
     * @param email       the user's email address
     * @param isGoogleSignup {@code true} if the account was created via Google OAuth
     * @return HTML string
     */
    public static String welcomeEmail(String name, String email, boolean isGoogleSignup) {
        String loginMethod = isGoogleSignup
                ? "You signed up using your <strong>Google account</strong> — no password required."
                : "You can now log in using your email and the password you chose.";

        String googleBadge = isGoogleSignup
                ? """
                  <div style="display:inline-flex;align-items:center;gap:8px;background:#fff;
                              border:1px solid #e5e7eb;border-radius:8px;padding:8px 14px;margin:16px 0;">
                    <svg width="18" height="18" viewBox="0 0 48 48">
                      <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                      <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                      <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
                      <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                    </svg>
                    <span style="font-size:13px;color:#374151;font-weight:500;">Signed up with Google</span>
                  </div>
                  """
                : "";

        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>👋 Welcome to Smart Campus!</h1>
                    <p>Your account has been created successfully</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>We're excited to have you on board. Your Smart Campus account is ready to use.</p>
                    %s
                    <div class="ticket-card">
                      <table>
                        <tr><td>Name</td><td>%s</td></tr>
                        <tr><td>Email</td><td>%s</td></tr>
                        <tr><td>Account Type</td><td>%s</td></tr>
                        <tr><td>Role</td><td><span class="badge badge-open">USER</span></td></tr>
                      </table>
                    </div>
                    <p>%s</p>
                    <p>With Smart Campus you can:</p>
                    <ul style="color:#374151;font-size:15px;line-height:1.8;padding-left:20px;">
                      <li>Submit and track maintenance &amp; incident tickets</li>
                      <li>Browse and reserve campus resources</li>
                      <li>Stay updated with real-time status notifications</li>
                    </ul>
                    <p>If you have any questions, reach out to the campus support team.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                name,
                googleBadge,
                name,
                email,
                isGoogleSignup ? "Google OAuth" : "Standard",
                loginMethod
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ticket Created
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to the reporter when a ticket is created.
     *
     * @param ticket the newly created incident ticket
     * @return HTML string
     */
    public static String ticketCreated(IncidentTicket ticket) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>🎫 Smart Campus — Ticket Received</h1>
                    <p>Maintenance &amp; Incident Tracking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your incident report has been received and is now in our system.
                       Our team will review and respond as soon as possible.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Ticket ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Category</td><td>%s</td></tr>
                        <tr><td>Location</td><td>%s</td></tr>
                        <tr><td>Priority</td><td>%s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-open">OPEN</span></td></tr>
                        <tr><td>Description</td><td>%s</td></tr>
                      </table>
                    </div>
                    <p>You will receive further updates as the status of your ticket changes.</p>
                    <p>Thank you for helping keep our campus in great shape!</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                ticket.getCreatedBy().getName(),
                ticket.getId(),
                ticket.getCategory(),
                ticket.getLocation(),
                ticket.getPriority(),
                ticket.getDescription()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Status Changed
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to the reporter when their ticket's status changes.
     *
     * @param ticket    the updated ticket (status already set to the new value)
     * @param oldStatus string representation of the previous status
     * @return HTML string
     */
    public static String statusChanged(IncidentTicket ticket, String oldStatus) {
        String newStatus = ticket.getStatus().name();
        String badgeClass = switch (ticket.getStatus()) {
            case IN_PROGRESS -> "badge-progress";
            case RESOLVED    -> "badge-resolved";
            case CLOSED      -> "badge-closed";
            case REJECTED    -> "badge-rejected";
            default          -> "badge-open";
        };

        String notesRow = (ticket.getResolutionNotes() != null && !ticket.getResolutionNotes().isBlank())
                ? "<tr><td>Notes</td><td>" + ticket.getResolutionNotes() + "</td></tr>"
                : "";

        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>🔔 Ticket Status Updated</h1>
                    <p>Maintenance &amp; Incident Tracking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>There has been an update to your incident ticket <strong>#%d</strong>.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Ticket ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Category</td><td>%s</td></tr>
                        <tr><td>Location</td><td>%s</td></tr>
                        <tr><td>Previous Status</td><td>%s</td></tr>
                        <tr><td>New Status</td><td><span class="badge %s">%s</span></td></tr>
                        %s
                      </table>
                    </div>
                    <p>If you have any questions, please contact the campus support team.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                ticket.getCreatedBy().getName(),
                ticket.getId(),
                ticket.getId(),
                ticket.getCategory(),
                ticket.getLocation(),
                oldStatus,
                badgeClass, newStatus,
                notesRow
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Technician Assigned
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to the technician when they are assigned to a ticket.
     *
     * @param ticket     the ticket they have been assigned to
     * @param technician the technician {@link User}
     * @return HTML string
     */
    public static String technicianAssigned(IncidentTicket ticket, User technician) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>🔧 New Ticket Assignment</h1>
                    <p>Maintenance &amp; Incident Tracking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>You have been assigned to the following incident ticket.
                       Please review the details and take action at the earliest opportunity.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Ticket ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Reported By</td><td>%s</td></tr>
                        <tr><td>Category</td><td>%s</td></tr>
                        <tr><td>Location</td><td>%s</td></tr>
                        <tr><td>Priority</td><td>%s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-progress">IN PROGRESS</span></td></tr>
                        <tr><td>Description</td><td>%s</td></tr>
                      </table>
                    </div>
                    <p>Please log in to the Smart Campus portal to update the ticket status
                       as you make progress.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                technician.getName(),
                ticket.getId(),
                ticket.getCreatedBy().getName(),
                ticket.getCategory(),
                ticket.getLocation(),
                ticket.getPriority(),
                ticket.getDescription()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin / Manager New Ticket
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to admins and managers when a new ticket is created.
     *
     * @param ticket the newly created incident ticket
     * @return HTML string
     */
    public static String adminNewTicket(IncidentTicket ticket) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>🚨 New Ticket Reported</h1>
                    <p>Maintenance &amp; Incident Tracking System</p>
                  </div>
                  <div class="body">
                    <p>Hello Admin/Manager,</p>
                    <p>A new incident report has been submitted by <strong>%s</strong>.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Ticket ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Category</td><td>%s</td></tr>
                        <tr><td>Location</td><td>%s</td></tr>
                        <tr><td>Priority</td><td>%s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-open">OPEN</span></td></tr>
                        <tr><td>Description</td><td>%s</td></tr>
                      </table>
                    </div>
                    <p>Please log in to the Smart Campus portal to review and assign a technician.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                ticket.getCreatedBy().getName(),
                ticket.getId(),
                ticket.getCategory(),
                ticket.getLocation(),
                ticket.getPriority(),
                ticket.getDescription()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin / Manager Comment Added
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to the ticket creator when an admin or
     * manager posts a comment on their ticket.
     *
     * @param ticket    the ticket that received the comment
     * @param comment   the newly added comment
     * @param commenter the admin/manager who wrote the comment
     * @return HTML string
     */
    public static String commentAdded(IncidentTicket ticket,
                                      com.project.paf.ticket.TicketComment comment,
                                      User commenter) {
        String badgeClass = switch (ticket.getStatus()) {
            case IN_PROGRESS -> "badge-progress";
            case RESOLVED    -> "badge-resolved";
            case CLOSED      -> "badge-closed";
            case REJECTED    -> "badge-rejected";
            default          -> "badge-open";
        };

        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>💬 New Comment on Your Ticket</h1>
                    <p>Maintenance &amp; Incident Tracking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p><strong>%s</strong> (Support Team) has left a comment on your ticket
                       <strong>#%d</strong>.</p>
                    <div class="ticket-card" style="border-left-color:#7c3aed;background:#f5f3ff;">
                      <table>
                        <tr><td>Ticket ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Category</td><td>%s</td></tr>
                        <tr><td>Location</td><td>%s</td></tr>
                        <tr><td>Current Status</td><td><span class="badge %s">%s</span></td></tr>
                      </table>
                    </div>
                    <p style="font-size:13px;color:#6b7280;margin:0 0 6px;">Comment:</p>
                    <div style="background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;
                                padding:16px 20px;margin-bottom:20px;">
                      <p style="margin:0;font-size:15px;color:#111827;white-space:pre-wrap;">%s</p>
                    </div>
                    <p>Log in to the Smart Campus portal to view the full conversation and
                       provide any additional information if needed.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                ticket.getCreatedBy().getName(),
                commenter.getName(),
                ticket.getId(),
                ticket.getId(),
                ticket.getCategory(),
                ticket.getLocation(),
                badgeClass, ticket.getStatus().name(),
                comment.getContent()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking — User Confirmation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to the user when they successfully create a booking.
     *
     * @param booking    the newly created booking
     * @param userName   the name of the user who created the booking
     * @return HTML string
     */
    public static String bookingCreatedUser(com.project.paf.modules.booking.entity.Booking booking, String userName) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>📅 Booking Request Received</h1>
                    <p>Smart Campus Resource Booking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your booking request has been received and is currently <strong>pending review</strong>.
                       You will be notified once it is confirmed by an administrator.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Resource</td><td>%s</td></tr>
                        <tr><td>Date</td><td>%s</td></tr>
                        <tr><td>Time</td><td>%s – %s</td></tr>
                        <tr><td>Reason</td><td>%s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-progress">PENDING</span></td></tr>
                      </table>
                    </div>
                    <p>Please log in to the Smart Campus portal to view or cancel your booking.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                userName,
                booking.getId(),
                booking.getResource(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getReason()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking — Admin / Manager Alert
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to admins/managers when a new booking is created.
     *
     * @param booking  the newly created booking
     * @param userName the name of the user who created the booking
     * @return HTML string
     */
    public static String adminNewBooking(com.project.paf.modules.booking.entity.Booking booking, String userName) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>🗓️ New Booking Request</h1>
                    <p>Smart Campus Resource Booking System</p>
                  </div>
                  <div class="body">
                    <p>Hello Admin/Manager,</p>
                    <p>A new resource booking has been submitted by <strong>%s</strong> and is awaiting your approval.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Requested By</td><td>%s</td></tr>
                        <tr><td>Resource</td><td>%s</td></tr>
                        <tr><td>Date</td><td>%s</td></tr>
                        <tr><td>Time</td><td>%s – %s</td></tr>
                        <tr><td>Reason</td><td>%s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-progress">PENDING</span></td></tr>
                      </table>
                    </div>
                    <p>Please log in to the Smart Campus portal to review and confirm or reject this booking.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                userName,
                booking.getId(),
                userName,
                booking.getResource(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getReason()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking — Confirmed
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to the user when their booking is confirmed.
     *
     * @param booking  the confirmed booking
     * @param userName the name of the user who owns the booking
     * @return HTML string
     */
    public static String bookingConfirmed(com.project.paf.modules.booking.entity.Booking booking, String userName) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>✅ Booking Confirmed</h1>
                    <p>Smart Campus Resource Booking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Great news! Your booking request has been <strong>confirmed</strong> by the campus administrator.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Resource</td><td>%s</td></tr>
                        <tr><td>Date</td><td>%s</td></tr>
                        <tr><td>Time</td><td>%s – %s</td></tr>
                        <tr><td>Reason</td><td>%s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-resolved">CONFIRMED</span></td></tr>
                      </table>
                    </div>
                    <p>Please arrive on time and respect campus resource usage guidelines. Thank you!</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                userName,
                booking.getId(),
                booking.getResource(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getReason()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin Created User — Credentials Email
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the HTML email body sent to a new user when an admin creates their account.
     * Contains their temporary password so they can log in immediately.
     *
     * @param name          the new user's display name
     * @param email         the new user's email address
     * @param tempPassword  the system-generated temporary password
     * @param role          the assigned role (e.g. "USER", "TECHNICIAN")
     * @return HTML string
     */
    public static String adminCreatedUser(String name, String email, String tempPassword, String role) {
        String roleBadgeClass = switch (role.toUpperCase()) {
            case "ADMIN"      -> "badge-resolved";
            case "MANAGER"    -> "badge-open";
            case "TECHNICIAN" -> "badge-progress";
            default           -> "badge-closed";
        };

        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>🏫 You've Been Added to Smart Campus</h1>
                    <p>Your account is ready — log in to get started</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>An administrator has created a Smart Campus account for you.
                       Your login credentials are below — please keep them safe and
                       <strong>change your password</strong> after your first login.</p>
                    <div class="ticket-card" style="border-left-color:#7c3aed;background:#faf5ff;">
                      <table>
                        <tr><td>Email</td><td><strong>%s</strong></td></tr>
                        <tr><td>Temporary Password</td><td>
                          <span style="font-family:monospace;font-size:16px;font-weight:700;
                                       letter-spacing:2px;background:#ede9fe;padding:4px 10px;
                                       border-radius:6px;color:#5b21b6;">%s</span>
                        </td></tr>
                        <tr><td>Role</td><td><span class="badge %s">%s</span></td></tr>
                      </table>
                    </div>
                    <p style="background:#fff7ed;border:1px solid #fed7aa;border-radius:8px;
                               padding:12px 16px;font-size:14px;color:#9a3412;">
                      ⚠ <strong>Important:</strong> This is a temporary password generated for your account.
                      Please log in and change it as soon as possible from your profile settings.
                    </p>
                    <p>You can also log in with Google if your email is a Gmail address — in that case
                       you don't need this password at all.</p>
                    <p>With Smart Campus you can:</p>
                    <ul style="color:#374151;font-size:15px;line-height:1.8;padding-left:20px;">
                      <li>Submit and track maintenance &amp; incident tickets</li>
                      <li>Browse and reserve campus resources</li>
                      <li>Stay updated with real-time status notifications</li>
                    </ul>
                    <p>If you have any questions, contact the campus support team.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                name,
                email,
                tempPassword,
                roleBadgeClass, role
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Booking — Cancelled
    // ─────────────────────────────────────────────────────────────────────────

    // -------------------------------------------------------------------------
    // Role Changed
    // -------------------------------------------------------------------------

    /**
     * Builds the HTML email body sent when an administrator updates a user's role.
     *
     * @param name    the user's display name
     * @param newRole the updated role assigned to the user
     * @return HTML string
     */
    public static String roleChanged(String name, String newRole) {
        String safeName = (name == null || name.isBlank()) ? "there" : name;
        String roleBadgeClass = switch (newRole.toUpperCase()) {
            case "ADMIN"      -> "badge-resolved";
            case "MANAGER"    -> "badge-open";
            case "TECHNICIAN" -> "badge-progress";
            default           -> "badge-closed";
        };

        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header">
                    <h1>Role Updated</h1>
                    <p>Your Smart Campus access has been updated</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>An administrator has updated your system role.</p>
                    <div class="ticket-card">
                      <table>
                        <tr><td>New Role</td><td><span class="badge %s">%s</span></td></tr>
                      </table>
                    </div>
                    <p>Your permissions in the Smart Campus system may have changed based on this update.</p>
                    <p>If you believe this change was made in error, please contact the campus support team.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                safeName,
                roleBadgeClass, newRole
        );
    }

    /**
     * Builds the HTML email body sent to the user when their booking is cancelled.
     *
     * @param booking       the cancelled booking
     * @param userName      the name of the user who owns the booking
     * @param cancelledBy   who cancelled it (e.g., "you" or "an administrator")
     * @return HTML string
     */
    public static String bookingCancelled(com.project.paf.modules.booking.entity.Booking booking, String userName, String cancelledBy) {
        return """
                <!DOCTYPE html><html><head>%s</head><body>
                <div class="wrapper">
                  <div class="header" style="background: linear-gradient(135deg, #7f1d1d 0%, #b91c1c 100%)">
                    <h1>❌ Booking Cancelled</h1>
                    <p>Smart Campus Resource Booking System</p>
                  </div>
                  <div class="body">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your booking has been <strong>cancelled</strong> by %s.</p>
                    <div class="ticket-card" style="border-left-color:#b91c1c;background:#fff5f5;">
                      <table>
                        <tr><td>Booking ID</td><td><strong>#%d</strong></td></tr>
                        <tr><td>Resource</td><td>%s</td></tr>
                        <tr><td>Date</td><td>%s</td></tr>
                        <tr><td>Time</td><td>%s – %s</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-rejected">CANCELLED</span></td></tr>
                      </table>
                    </div>
                    <p>If you believe this was done in error or have any questions, please contact the campus support team.</p>
                  </div>
                  <div class="footer">
                    <p>Smart Campus · IT3030 Project · This is an automated notification — please do not reply.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(
                STYLE,
                userName,
                cancelledBy,
                booking.getId(),
                booking.getResource(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
}
