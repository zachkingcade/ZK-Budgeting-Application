package zachkingcade.dev.reporting.application.report;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Signed running totals per account from journal entries (minor units), using {@code lineAffectOnAccount}.
 */
public final class JournalBalanceAggregator {

    private JournalBalanceAggregator() {
    }

    /**
     * @param inclusiveEnd only entries on or before this date are included
     * @param lineFilter if false for a line, that line is skipped (e.g. account / type scoping)
     */
    public static Map<Long, Long> signedBalancesThrough(
            JsonNode journalEntryList,
            LocalDate inclusiveEnd,
            Predicate<JsonNode> lineFilter
    ) {
        Map<Long, Long> totals = new HashMap<>();
        if (journalEntryList == null || !journalEntryList.isArray()) {
            return totals;
        }
        for (JsonNode entry : journalEntryList) {
            LocalDate d = LocalDate.parse(entry.get("entryDate").asText());
            if (d.isAfter(inclusiveEnd)) {
                continue;
            }
            JsonNode lines = entry.path("journalLines");
            if (!lines.isArray()) {
                continue;
            }
            for (JsonNode line : lines) {
                if (lineFilter != null && !lineFilter.test(line)) {
                    continue;
                }
                long accountId = line.path("accountId").asLong();
                long amount = line.path("amount").asLong();
                String aff = line.path("lineAffectOnAccount").asText("+");
                long signed = "+".equals(aff) ? amount : -amount;
                totals.merge(accountId, signed, Long::sum);
            }
        }
        return totals;
    }

    /**
     * Sum signed line amounts in the given entries (no date filter), optionally restricted to account ids.
     */
    public static Map<Long, Long> signedBalancesForLinesInEntries(
            JsonNode journalEntryList,
            Predicate<JsonNode> lineFilter
    ) {
        Map<Long, Long> totals = new HashMap<>();
        if (journalEntryList == null || !journalEntryList.isArray()) {
            return totals;
        }
        for (JsonNode entry : journalEntryList) {
            JsonNode lines = entry.path("journalLines");
            if (!lines.isArray()) {
                continue;
            }
            for (JsonNode line : lines) {
                if (lineFilter != null && !lineFilter.test(line)) {
                    continue;
                }
                long accountId = line.path("accountId").asLong();
                long amount = line.path("amount").asLong();
                String aff = line.path("lineAffectOnAccount").asText("+");
                long signed = "+".equals(aff) ? amount : -amount;
                totals.merge(accountId, signed, Long::sum);
            }
        }
        return totals;
    }

    public static long signedAmountForLine(JsonNode line) {
        long amount = line.path("amount").asLong();
        String aff = line.path("lineAffectOnAccount").asText("+");
        return "+".equals(aff) ? amount : -amount;
    }

    public static Predicate<JsonNode> lineMatchesParameterFilter(
            Set<Long> accountIdFilterOrEmpty,
            Set<Long> accountTypeIdFilterOrEmpty,
            Map<Long, Long> accountIdToTypeId
    ) {
        return line -> {
            long aid = line.path("accountId").asLong();
            if (accountIdFilterOrEmpty != null && !accountIdFilterOrEmpty.isEmpty()) {
                if (!accountIdFilterOrEmpty.contains(aid)) {
                    return false;
                }
            }
            if (accountTypeIdFilterOrEmpty != null && !accountTypeIdFilterOrEmpty.isEmpty()) {
                Long tid = accountIdToTypeId.get(aid);
                if (tid == null || !accountTypeIdFilterOrEmpty.contains(tid)) {
                    return false;
                }
            }
            return true;
        };
    }
}
