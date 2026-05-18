package zachkingcade.dev.ledger.application.accountingperiod;

import org.springframework.stereotype.Component;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.port.out.usersetting.UserSettingsClientPort;
import zachkingcade.dev.ledger.domain.accountingperiod.AccountingPeriodConfig;
import zachkingcade.dev.ledger.domain.accountingperiod.AccountingPeriodFrequencyUnit;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
public class AccountingPeriodSettingsResolver {

    private final UserSettingsClientPort userSettingsClient;

    public AccountingPeriodSettingsResolver(UserSettingsClientPort userSettingsClient) {
        this.userSettingsClient = userSettingsClient;
    }

    public Optional<AccountingPeriodConfig> resolve(Long userId) {
        Map<String, String> settings = userSettingsClient.fetchSettingsByName(userId);
        String unitValue = settings.get(AccountingPeriodSettingKeys.FREQUENCY_UNIT);
        String countValue = settings.get(AccountingPeriodSettingKeys.FREQUENCY_COUNT);
        String startValue = settings.get(AccountingPeriodSettingKeys.FIRST_START_DATE);

        if (unitValue == null || countValue == null || startValue == null) {
            return Optional.empty();
        }

        try {
            AccountingPeriodFrequencyUnit unit = AccountingPeriodFrequencyUnit.fromSetting(unitValue);
            int count = Integer.parseInt(countValue.trim());
            LocalDate firstStart = LocalDate.parse(startValue.trim());
            validateCount(unit, count);
            return Optional.of(new AccountingPeriodConfig(firstStart, unit, count));
        } catch (RuntimeException ex) {
            throw new ApplicationException("Invalid accounting period settings: " + ex.getMessage());
        }
    }

    static void validateCount(AccountingPeriodFrequencyUnit unit, int count) {
        if (count < 1) {
            throw new ApplicationException("frequency count must be at least 1");
        }
        int max = switch (unit) {
            case DAYS -> 365;
            case WEEKS -> 52;
            case MONTHS -> 12;
        };
        if (count > max) {
            throw new ApplicationException("frequency count exceeds maximum for unit");
        }
    }
}
