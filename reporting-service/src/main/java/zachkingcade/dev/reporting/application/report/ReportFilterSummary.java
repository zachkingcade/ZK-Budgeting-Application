package zachkingcade.dev.reporting.application.report;

import com.fasterxml.jackson.databind.JsonNode;
import zachkingcade.dev.reporting.adapter.outbound.LedgerApiClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds human-readable "filters applied" lines for PDFs (resolves account and account type ids).
 */
public final class ReportFilterSummary {

    private static final DateTimeFormatter FILTER_DATE =
            DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(Locale.US);

    private ReportFilterSummary() {
    }

    public static List<String> buildLines(JsonNode parameters, LedgerApiClient ledger, String bearerToken)
            throws Exception {
        List<String> out = new ArrayList<>();
        if (parameters == null || parameters.isNull()) {
            return out;
        }

        if (parameters.hasNonNull("dateFrom") && parameters.hasNonNull("dateTo")) {
            LocalDate from = LocalDate.parse(parameters.get("dateFrom").asText());
            LocalDate to = LocalDate.parse(parameters.get("dateTo").asText());
            out.add("Date range: " + FILTER_DATE.format(from) + " – " + FILTER_DATE.format(to));
        } else if (parameters.hasNonNull("dateFrom")) {
            out.add("From: " + FILTER_DATE.format(LocalDate.parse(parameters.get("dateFrom").asText())));
        } else if (parameters.hasNonNull("dateTo")) {
            out.add("Through: " + FILTER_DATE.format(LocalDate.parse(parameters.get("dateTo").asText())));
        }

        if (parameters.hasNonNull("asOfDate")) {
            out.add("As of: " + FILTER_DATE.format(LocalDate.parse(parameters.get("asOfDate").asText())));
        }

        Map<Long, String> accountNames = loadAccountNames(ledger, bearerToken);
        if (parameters.has("accountIds") && parameters.get("accountIds").isArray() && !parameters.get("accountIds").isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (JsonNode n : parameters.get("accountIds")) {
                ids.add(n.asLong());
            }
            ids.sort(Comparator.naturalOrder());
            List<String> names = new ArrayList<>();
            for (Long id : ids) {
                names.add(accountNames.getOrDefault(id, "Account #" + id));
            }
            out.add("Accounts: " + String.join(", ", names));
        }

        Map<Long, String> typeNames = loadAccountTypeNames(ledger, bearerToken);
        if (parameters.has("accountTypeIds") && parameters.get("accountTypeIds").isArray()
                && !parameters.get("accountTypeIds").isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (JsonNode n : parameters.get("accountTypeIds")) {
                ids.add(n.asLong());
            }
            ids.sort(Comparator.naturalOrder());
            List<String> names = new ArrayList<>();
            for (Long id : ids) {
                names.add(typeNames.getOrDefault(id, "Account type #" + id));
            }
            out.add("Account types: " + String.join(", ", names));
        }

        if (parameters.has("includeJournalLines")) {
            JsonNode flag = parameters.get("includeJournalLines");
            if (flag.isBoolean()) {
                out.add("Include journal lines: " + (flag.booleanValue() ? "Yes" : "No"));
            }
        }
        if (parameters.has("newPagePerAccount")) {
            JsonNode flag = parameters.get("newPagePerAccount");
            if (flag.isBoolean()) {
                out.add("New page per account: " + (flag.booleanValue() ? "Yes" : "No"));
            }
        }

        return out;
    }

    private static Map<Long, String> loadAccountNames(LedgerApiClient ledger, String token) throws Exception {
        Map<Long, String> map = new HashMap<>();
        JsonNode list = ledger.getAccountsAll(token).path("accountsList");
        if (!list.isArray()) {
            return map;
        }
        for (JsonNode a : list) {
            long id = a.path("accountId").asLong();
            String name = text(a, "accountDisplayName");
            map.put(id, name.isEmpty() ? "Account #" + id : name);
        }
        return map;
    }

    private static Map<Long, String> loadAccountTypeNames(LedgerApiClient ledger, String token) throws Exception {
        Map<Long, String> map = new HashMap<>();
        JsonNode list = ledger.getAccountTypesAll(token).path("accountTypeList");
        if (!list.isArray()) {
            return map;
        }
        for (JsonNode t : list) {
            long id = t.path("id").asLong();
            String desc = text(t, "description");
            map.put(id, desc.isEmpty() ? "Type #" + id : desc);
        }
        return map;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText();
    }

    /**
     * accountId -> typeId for line-level filtering.
     */
    public static Map<Long, Long> accountIdToTypeId(LedgerApiClient ledger, String token) throws Exception {
        Map<Long, Long> map = new HashMap<>();
        JsonNode list = ledger.getAccountsAll(token).path("accountsList");
        if (!list.isArray()) {
            return map;
        }
        for (JsonNode a : list) {
            map.put(a.path("accountId").asLong(), a.path("typeId").asLong());
        }
        return map;
    }
}
