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
import zachkingcade.dev.user.application.exception.ConflictException;
import zachkingcade.dev.user.application.exception.NotFoundException;
import zachkingcade.dev.user.application.port.in.usersetting.UserSettingsUseCase;
import zachkingcade.dev.user.config.JwtDecoderConfig;
import zachkingcade.dev.user.config.SecurityConfig;
import zachkingcade.dev.user.domain.usersetting.UserSetting;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSettingsController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtDecoderConfig.class})
class UserSettingsControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserSettingsUseCase userSettingsUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldListSettings() throws Exception {
        when(userSettingsUseCase.listAll(1L)).thenReturn(List.of(
                new UserSetting(10L, 1L, "accounting_period.frequency_unit", "MONTHS", Instant.EPOCH, null)));

        mvc.perform(get("/user/settings").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].settingName").value("accounting_period.frequency_unit"));
    }

    @Test
    void shouldCreateSetting() throws Exception {
        when(userSettingsUseCase.create(eq(1L), eq("foo"), eq("bar")))
                .thenReturn(new UserSetting(1L, 1L, "foo", "bar", Instant.EPOCH, null));

        mvc.perform(post("/user/settings")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"settingName":"foo","settingValue":"bar"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.settingName").value("foo"));
    }

    @Test
    void shouldReturnConflictWhenDuplicateName() throws Exception {
        when(userSettingsUseCase.create(eq(1L), eq("foo"), eq("bar")))
                .thenThrow(new ConflictException("Setting name already exists for this user"));

        mvc.perform(post("/user/settings")
                        .with(jwt().jwt(userJwt(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"settingName":"foo","settingValue":"bar"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldDeleteByName() throws Exception {
        mvc.perform(delete("/user/settings/by-name/foo").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isOk());

        verify(userSettingsUseCase).deleteByName(1L, "foo");
    }

    @Test
    void shouldReturnNotFoundWhenMissing() throws Exception {
        doThrow(new NotFoundException("Setting not found")).when(userSettingsUseCase).getByName(1L, "missing");

        mvc.perform(get("/user/settings/by-name/missing").with(jwt().jwt(userJwt(1L))))
                .andExpect(status().isNotFound());
    }

    private static Jwt userJwt(Long userId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .claim("token_type", "user")
                .claim("aud", List.of("user-service"))
                .issuer("auth-service")
                .build();
    }
}
