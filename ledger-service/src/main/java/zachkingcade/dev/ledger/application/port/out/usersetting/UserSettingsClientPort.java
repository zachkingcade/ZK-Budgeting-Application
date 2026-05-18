package zachkingcade.dev.ledger.application.port.out.usersetting;

import java.util.Map;

public interface UserSettingsClientPort {

    Map<String, String> fetchSettingsByName(Long userId);
}
