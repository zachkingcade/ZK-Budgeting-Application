package zachkingcade.dev.user.adapter.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.user.adapter.web.dto.ApiResponse;
import zachkingcade.dev.user.adapter.web.dto.MetaData;
import zachkingcade.dev.user.adapter.web.dto.usersetting.CreateUserSettingRequest;
import zachkingcade.dev.user.adapter.web.dto.usersetting.UpdateUserSettingValueRequest;
import zachkingcade.dev.user.adapter.web.dto.usersetting.UserSettingResponse;
import zachkingcade.dev.user.application.port.in.usersetting.UserSettingsUseCase;
import zachkingcade.dev.user.domain.usersetting.UserSetting;

import java.util.List;

@RestController
@RequestMapping("/user/settings")
public class UserSettingsController {

    private final UserSettingsUseCase userSettingsUseCase;

    public UserSettingsController(UserSettingsUseCase userSettingsUseCase) {
        this.userSettingsUseCase = userSettingsUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSettingResponse>>> list(@AuthenticationPrincipal Jwt jwt) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        List<UserSettingResponse> data = userSettingsUseCase.listAll(userId).stream()
                .map(UserSettingResponse::from)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>("Listed user settings", new MetaData((long) data.size()), data));
    }

    @GetMapping("/{settingId}")
    public ResponseEntity<ApiResponse<UserSettingResponse>> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long settingId) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        UserSetting setting = userSettingsUseCase.getById(userId, settingId);
        return ResponseEntity.ok(new ApiResponse<>("Found setting", new MetaData(1L), UserSettingResponse.from(setting)));
    }

    @GetMapping("/by-name/{name:.+}")
    public ResponseEntity<ApiResponse<UserSettingResponse>> getByName(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("name") String name) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        UserSetting setting = userSettingsUseCase.getByName(userId, name);
        return ResponseEntity.ok(new ApiResponse<>("Found setting", new MetaData(1L), UserSettingResponse.from(setting)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserSettingResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateUserSettingRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        UserSetting created = userSettingsUseCase.create(userId, request.settingName(), request.settingValue());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Created setting", new MetaData(1L), UserSettingResponse.from(created)));
    }

    @PutMapping("/by-name/{name:.+}")
    public ResponseEntity<ApiResponse<UserSettingResponse>> updateByName(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("name") String name,
            @Valid @RequestBody UpdateUserSettingValueRequest request) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        UserSetting updated = userSettingsUseCase.updateByName(userId, name, request.settingValue());
        return ResponseEntity.ok(new ApiResponse<>("Updated setting", new MetaData(1L), UserSettingResponse.from(updated)));
    }

    @DeleteMapping("/{settingId}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long settingId) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        userSettingsUseCase.deleteById(userId, settingId);
        return ResponseEntity.ok(new ApiResponse<>("Deleted setting", new MetaData(0L), null));
    }

    @DeleteMapping("/by-name/{name:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteByName(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("name") String name) {
        Long userId = JwtPrincipalUserIdExtractor.extractUserId(jwt);
        userSettingsUseCase.deleteByName(userId, name);
        return ResponseEntity.ok(new ApiResponse<>("Deleted setting", new MetaData(0L), null));
    }
}
