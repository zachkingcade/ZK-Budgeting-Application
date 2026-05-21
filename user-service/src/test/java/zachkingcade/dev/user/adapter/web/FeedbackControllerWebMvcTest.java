package zachkingcade.dev.user.adapter.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zachkingcade.dev.user.adapter.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.user.application.RolesService;
import zachkingcade.dev.user.application.port.in.feedback.FeedbackUseCase;
import zachkingcade.dev.user.config.JwtDecoderConfig;
import zachkingcade.dev.user.config.SecurityConfig;
import zachkingcade.dev.user.domain.feedback.UserFeedback;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtDecoderConfig.class})
class FeedbackControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FeedbackUseCase feedbackUseCase;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldSubmitFeedbackForUser() throws Exception {
        when(feedbackUseCase.submit(eq(1L), eq("bug"), eq("Broken"), eq("Details")))
                .thenReturn(new UserFeedback(1L, "bug", "Broken", "Details", 1L, "alice", Instant.EPOCH, false));

        mvc.perform(post("/user/feedback")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"bug","title":"Broken","message":"Details"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("bug"));
    }

    @Test
    void shouldListFeedbackForAdmin() throws Exception {
        when(rolesService.isAdmin(1L)).thenReturn(true);
        when(feedbackUseCase.listByType("bug")).thenReturn(List.of(
                new UserFeedback(2L, "bug", "T", "M", 3L, "bob", Instant.EPOCH, false)));

        mvc.perform(get("/user/feedback?type=bug").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("bob"));
    }

    @Test
    void shouldRejectListForNonAdmin() throws Exception {
        when(rolesService.isAdmin(1L)).thenReturn(false);

        mvc.perform(get("/user/feedback?type=bug").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldMarkSeenForAdmin() throws Exception {
        when(rolesService.isAdmin(1L)).thenReturn(true);
        when(feedbackUseCase.markSeen(5L))
                .thenReturn(new UserFeedback(5L, "suggestion", "T", "M", 2L, "alice", Instant.EPOCH, true));

        mvc.perform(patch("/user/feedback/5/seen").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.seen").value(true));
    }

    @Test
    void shouldDeleteForAdmin() throws Exception {
        when(rolesService.isAdmin(1L)).thenReturn(true);

        mvc.perform(delete("/user/feedback/9").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk());

        verify(feedbackUseCase).delete(9L);
    }

    private static Jwt userJwt(long userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("token_type", "user")
                .claim("aud", List.of("user-service"))
                .build();
    }
}
