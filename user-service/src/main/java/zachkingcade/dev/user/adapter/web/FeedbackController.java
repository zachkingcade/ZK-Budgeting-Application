package zachkingcade.dev.user.adapter.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import zachkingcade.dev.user.adapter.web.dto.feedback.FeedbackResponse;
import zachkingcade.dev.user.adapter.web.dto.feedback.SubmitFeedbackRequest;
import zachkingcade.dev.user.application.RolesService;
import zachkingcade.dev.user.application.port.in.feedback.FeedbackUseCase;
import zachkingcade.dev.user.domain.feedback.UserFeedback;

import java.util.List;

@RestController
@RequestMapping("/user/feedback")
public class FeedbackController {

    private final FeedbackUseCase feedbackUseCase;
    private final RolesService rolesService;

    public FeedbackController(FeedbackUseCase feedbackUseCase, RolesService rolesService) {
        this.feedbackUseCase = feedbackUseCase;
        this.rolesService = rolesService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        UserFeedback created = feedbackUseCase.submit(
                userId,
                request.type(),
                request.title(),
                request.message());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Feedback submitted", new MetaData(1L), FeedbackResponse.from(created)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String type) {
        requireAdmin(jwt);
        List<FeedbackResponse> data = feedbackUseCase.listByType(type).stream()
                .map(FeedbackResponse::from)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>("Listed feedback", new MetaData((long) data.size()), data));
    }

    @PatchMapping("/{id}/seen")
    public ResponseEntity<ApiResponse<FeedbackResponse>> markSeen(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        requireAdmin(jwt);
        UserFeedback updated = feedbackUseCase.markSeen(id);
        return ResponseEntity.ok(new ApiResponse<>("Marked feedback seen", new MetaData(1L), FeedbackResponse.from(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        requireAdmin(jwt);
        feedbackUseCase.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("Deleted feedback", new MetaData(0L), null));
    }

    private void requireAdmin(Jwt jwt) {
        Long userId = JwtAuthSupport.requireUserId(jwt);
        if (!rolesService.isAdmin(userId)) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
