package zachkingcade.dev.user.adapter.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import zachkingcade.dev.user.adapter.web.dto.GlobalExceptionHandler;
import zachkingcade.dev.user.application.exception.ApplicationException;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.Session.RefreshSessionUseCase;
import zachkingcade.dev.user.application.port.in.service.ServiceLoginUseCase;
import zachkingcade.dev.user.application.port.in.user.LoginUserUseCase;
import zachkingcade.dev.user.application.port.in.user.LogoutUserUseCase;
import zachkingcade.dev.user.application.port.in.user.RegisterUserUseCase;
import zachkingcade.dev.user.application.results.LogInUserResult;
import zachkingcade.dev.user.application.results.RefreshSessionResult;
import zachkingcade.dev.user.application.results.ServiceLoginResult;
import zachkingcade.dev.user.config.SecurityConfig;
import zachkingcade.dev.user.domain.user.User;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private LoginUserUseCase loginUserUseCase;

    @MockitoBean
    private LogoutUserUseCase logoutUserUseCase;

    @MockitoBean
    private RefreshSessionUseCase refreshSessionUseCase;

    @MockitoBean
    private ServiceLoginUseCase serviceLoginUseCase;

    @Test
    void shouldRegisterUserWhenRequestValid() throws Exception {
        // HAPPY PATH
        when(registerUserUseCase.registerUser(any()))
                .thenReturn(User.rehydrate(123L, "alice", true, Instant.EPOCH, Instant.EPOCH));

        mvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"Password123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusMessage").value("Created User [alice]"))
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterUserUsernameExists() throws Exception {
        /*
        NEGATIVE PATH: method=POST /user/register,
        input={username: existing, password: any},
        expected failure message=Username already exists
        */
        when(registerUserUseCase.registerUser(any()))
                .thenThrow(new ApplicationException("Username already exists"));

        mvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"Password123!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("APPLICATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void shouldLoginUserWhenCredentialsValid() throws Exception {
        // HAPPY PATH
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(loginUserUseCase.loginUser(any()))
                .thenReturn(new LogInUserResult(
                        "alice",
                        "session-token",
                        now,
                        now.plusSeconds(3600),
                        "access-token",
                        now,
                        now.plusSeconds(900)
                ));

        mvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"Password123!"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.sessionToken").value("session-token"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void shouldReturnNotFoundWhenLoginUserMissingUser() throws Exception {
        /*
        NEGATIVE PATH: method=POST /user/login,
        input={username: missing, password: any},
        expected failure message=User not found
        */
        when(loginUserUseCase.loginUser(any()))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"missing","password":"Password123!"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void shouldLogoutUserWhenRequestValid() throws Exception {
        // HAPPY PATH
        mvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","sessionToken":"session-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("alice"));
    }

    @Test
    void shouldReturnNotFoundWhenLogoutUserSessionMissing() throws Exception {
        /*
        NEGATIVE PATH: method=POST /user/logout,
        input={username: alice, sessionToken: missing},
        expected failure message=Session not found
        */
        doThrow(new NotFoundException("Session not found"))
                .when(logoutUserUseCase)
                .logoutUser(any());

        mvc.perform(post("/user/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","sessionToken":"missing"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Session not found"));
    }

    @Test
    void shouldRefreshSessionWhenUnexpired() throws Exception {
        // HAPPY PATH
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(refreshSessionUseCase.refreshSession(any()))
                .thenReturn(new RefreshSessionResult(true, "new-access-token", now, now.plusSeconds(900)));

        mvc.perform(post("/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","sessionToken":"session-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("Session Refreshed, new accessToken provided."))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void shouldReturnOkWhenSessionExpired() throws Exception {
        /*
        NEGATIVE PATH: method=POST /user/refresh,
        input={username: alice, sessionToken: expired},
        expected failure message=Session expired, please login again.
        */
        when(refreshSessionUseCase.refreshSession(any()))
                .thenReturn(new RefreshSessionResult(false, null, null, null));

        mvc.perform(post("/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","sessionToken":"expired"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("Session expired, please login again."))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist());
    }

    @Test
    void shouldLoginServiceWhenValid() throws Exception {
        // HAPPY PATH
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(serviceLoginUseCase.loginService(any()))
                .thenReturn(new ServiceLoginResult("svc-token", now, now.plusSeconds(900)));

        mvc.perform(post("/user/service/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serviceName":"reporting-service","secret":"password","actingForUserId":1,"audiences":["ledger-service"],"scopes":["ledger.accounts.read"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("svc-token"));
    }

    @Test
    void shouldReturnBadRequestWhenServiceLoginValidationFails() throws Exception {
        /*
        NEGATIVE PATH: method=POST /user/service/login,
        input={serviceName: blank, secret: blank},
        expected failure message=must not be blank
        */
        mvc.perform(post("/user/service/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serviceName":" ","secret":" "}
                                """))
                .andExpect(status().isBadRequest());
    }
}

