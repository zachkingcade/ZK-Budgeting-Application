package zachkingcade.dev.user.domain.usersetting;

import java.time.Instant;

public record UserSetting(
        Long settingId,
        Long userId,
        String settingName,
        String settingValue,
        Instant createdDate,
        Instant updatedDate
) {
}
