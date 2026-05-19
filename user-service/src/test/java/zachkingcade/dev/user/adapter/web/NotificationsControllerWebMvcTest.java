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
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.notification.NotificationsUseCase;
import zachkingcade.dev.user.config.JwtDecoderConfig;
import zachkingcade.dev.user.config.SecurityConfig;
import zachkingcade.dev.user.domain.notification.Notification;

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

@WebMvcTest(NotificationsController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtDecoderConfig.class})
class NotificationsControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NotificationsUseCase notificationsUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldListNotificationsForUser() throws Exception {
        when(notificationsUseCase.listForUser(1L, null)).thenReturn(List.of(
                new Notification(5L, 1L, Instant.EPOCH, "reports", "Report ready", "Your report is ready.", false)));

        mvc.perform(get("/user/notifications").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Report ready"));
    }

    @Test
    void shouldRegisterNotificationWithServiceToken() throws Exception {
        when(notificationsUseCase.register(eq(2L), eq("reports"), eq("Report ready"), eq("Done")))
                .thenReturn(new Notification(1L, 2L, Instant.EPOCH, "reports", "Report ready", "Done", false));

        mvc.perform(post("/user/notifications/register")
                        .with(jwt().jwt(serviceJwt()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":2,"system":"reports","title":"Report ready","message":"Done"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.system").value("reports"));
    }

    @Test
    void shouldRejectRegisterForUserToken() throws Exception {
        mvc.perform(post("/user/notifications/register")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":2,"system":"reports","title":"Report ready","message":"Done"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAdminListForUserToken() throws Exception {
        mvc.perform(get("/user/notifications/admin").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListAdminForServiceToken() throws Exception {
        when(notificationsUseCase.listAdmin(null)).thenReturn(List.of(
                new Notification(1L, -1L, Instant.EPOCH, "user-service", "New account registered", "alice", false)));

        mvc.perform(get("/user/notifications/admin").with(jwt().jwt(serviceJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(-1));
    }

    @Test
    void shouldMarkSeen() throws Exception {
        when(notificationsUseCase.markSeen(1L, 9L))
                .thenReturn(new Notification(9L, 1L, Instant.EPOCH, "reports", "T", "M", true));

        mvc.perform(patch("/user/notifications/9/seen").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.seen").value(true));
    }

    @Test
    void shouldClearByIds() throws Exception {
        mvc.perform(post("/user/notifications/clear")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ids":[1,2,3]}
                                """))
                .andExpect(status().isOk());

        verify(notificationsUseCase).clearByIds(1L, List.of(1L, 2L, 3L));
    }

    @Test
    void shouldClearAll() throws Exception {
        mvc.perform(delete("/user/notifications/clearall").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk());

        verify(notificationsUseCase).clearAll(1L);
    }

    @Test
    void shouldDeleteNotification() throws Exception {
        mvc.perform(delete("/user/notifications/4").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk());

        verify(notificationsUseCase).delete(1L, 4L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissing() throws Exception {
        org.mockito.Mockito.doThrow(new NotFoundException("Notification not found"))
                .when(notificationsUseCase).delete(1L, 99L);

        mvc.perform(delete("/user/notifications/99").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isNotFound());
    }

    private static Jwt userJwt(long userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("token_type", "user")
                .claim("aud", List.of("user-service"))
                .build();
    }

    private static Jwt serviceJwt() {
        return Jwt.withTokenValue("service-token")
                .header("alg", "none")
                .subject("reporting-service")
                .claim("token_type", "service")
                .claim("scope", "notifications.write")
                .claim("aud", List.of("user-service"))
                .build();
    }
}
