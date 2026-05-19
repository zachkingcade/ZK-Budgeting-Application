package zachkingcade.dev.user.adapter.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.user.adapter.web.dto.ApiResponse;
import zachkingcade.dev.user.adapter.web.dto.MetaData;
import zachkingcade.dev.user.adapter.web.dto.notification.ClearNotificationsRequest;
import zachkingcade.dev.user.adapter.web.dto.notification.NotificationResponse;
import zachkingcade.dev.user.adapter.web.dto.notification.RegisterNotificationRequest;
import zachkingcade.dev.user.application.port.in.notification.NotificationsUseCase;
import zachkingcade.dev.user.domain.notification.Notification;

import java.util.List;

@RestController
@RequestMapping("/user/notifications")
public class NotificationsController {

    private static final String NOTIFICATIONS_WRITE_SCOPE = "notifications.write";

    private final NotificationsUseCase notificationsUseCase;

    public NotificationsController(NotificationsUseCase notificationsUseCase) {
        this.notificationsUseCase = notificationsUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<NotificationResponse>> register(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegisterNotificationRequest request) {
        JwtAuthSupport.requireServiceScope(jwt, NOTIFICATIONS_WRITE_SCOPE);
        Notification created = notificationsUseCase.register(
                request.userId(),
                request.system(),
                request.title(),
                request.message());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Registered notification", new MetaData(1L), NotificationResponse.from(created)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer limit) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        List<NotificationResponse> data = notificationsUseCase.listForUser(userId, limit).stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>("Listed notifications", new MetaData((long) data.size()), data));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer limit) {
        JwtAuthSupport.requireServiceToken(jwt);
        List<NotificationResponse> data = notificationsUseCase.listAdmin(limit).stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>("Listed admin notifications", new MetaData((long) data.size()), data));
    }

    @PatchMapping("/{id}/seen")
    public ResponseEntity<ApiResponse<NotificationResponse>> markSeen(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        Notification updated = notificationsUseCase.markSeen(userId, id);
        return ResponseEntity.ok(new ApiResponse<>("Marked notification seen", new MetaData(1L), NotificationResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        notificationsUseCase.delete(userId, id);
        return ResponseEntity.ok(new ApiResponse<>("Deleted notification", new MetaData(0L), null));
    }

    @PostMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clear(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ClearNotificationsRequest request) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        notificationsUseCase.clearByIds(userId, request.ids());
        return ResponseEntity.ok(new ApiResponse<>("Cleared notifications", new MetaData((long) request.ids().size()), null));
    }

    @DeleteMapping("/clearall")
    public ResponseEntity<ApiResponse<Void>> clearAll(@AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        notificationsUseCase.clearAll(userId);
        return ResponseEntity.ok(new ApiResponse<>("Cleared all notifications", new MetaData(0L), null));
    }
}
