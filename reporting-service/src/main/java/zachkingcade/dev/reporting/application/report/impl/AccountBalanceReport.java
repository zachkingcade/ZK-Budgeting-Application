package zachkingcade.dev.reporting.application.report.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import zachkingcade.dev.reporting.adapter.outbound.LedgerApiClient;
import zachkingcade.dev.reporting.application.report.JournalBalanceAggregator;
import zachkingcade.dev.reporting.application.report.Report;
import zachkingcade.dev.reporting.application.report.ReportContext;
import zachkingcade.dev.reporting.application.report.ReportCurrencyFormat;
import zachkingcade.dev.reporting.application.report.ReportFilterSummary;
import zachkingcade.dev.reporting.application.report.ReportPdfLayout;
import zachkingcade.dev.reporting.application.report.ReportPdfLayout.TableBlock;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Component
public class AccountBalanceReport implements Report {

    public static final String CODE = "ACCOUNT_BALANCE";

    private static final DateTimeFormatter SUBTITLE_DATE =
            DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(Locale.US);

    private static final float[] COL_WIDTHS = {72f, 28f};

    private final ObjectMapper objectMapper;

    public AccountBalanceReport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public ObjectNode catalogJson() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("code", CODE);
        root.put("displayName", "Account Balance Report");
        root.put("description", "Shows balances per account, optionally as of a given date.");
        var fields = objectMapper.createArrayNode();
        fields.add(field("asOfDate", "As-of date (optional)", "date", "If omitted, uses current balances from the ledger.", false));
        fields.add(field("accountIds", "Accounts (optional)", "account-multi", "If omitted, includes all accounts.", false));
        fields.add(field("accountTypeIds", "Account types (optional)", "account-type-multi", "If omitted, includes all account types.", false));
        root.set("fields", fields);
        return root;
    }

    private ObjectNode field(String name, String description, String type, String desc2, boolean required) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("description", description + " " + desc2);
        node.put("type", type);
        node.put("required", required);
        return node;
    }

    @Override
    public void validateParameters(JsonNode parameters) {
        if (parameters != null && parameters.hasNonNull("asOfDate")) {
            LocalDate.parse(parameters.get("asOfDate").asText());
        }
    }

    @Override
    public byte[] generatePdf(ReportContext context) throws Exception {
        String token = context.ledgerToken();
        LedgerApiClient ledger = context.ledgerApiClient();
        JsonNode params = context.parameters() == null ? objectMapper.createObjectNode() : context.parameters();

        JsonNode accountsResponse = ledger.getAccountsAll(token);
        JsonNode accountsList = accountsResponse.path("accountsList");
        if (!accountsList.isArray()) {
            accountsList = objectMapper.createArrayNode();
        }

        Set<Long> includedAccountIds = new HashSet<>();
        for (JsonNode a : accountsList) {
            if (shouldInclude(a, params)) {
                includedAccountIds.add(a.path("accountId").asLong());
            }
        }

        List<String[]> rows = new ArrayList<>();
        LocalDate asOf = params.hasNonNull("asOfDate") ? LocalDate.parse(params.get("asOfDate").asText()) : null;

        long sumBalancesMinor = 0L;
        if (asOf == null) {
            for (JsonNode a : accountsList) {
                if (shouldInclude(a, params)) {
                    long bal = a.path("accountBalance").asLong(0L);
                    sumBalancesMinor += bal;
                    rows.add(new String[]{
                            text(a, "accountDisplayName"),
                            ReportCurrencyFormat.formatMinorUnits(bal)
                    });
                }
            }
        } else {
            JsonNode journalBody = buildJournalFilter(asOf, params);
            JsonNode journalData = ledger.postJournalFiltered(token, journalBody);
            JsonNode journalEntries = journalData.path("journalEntryList");
            Predicate<JsonNode> lineFilter =
                    line -> includedAccountIds.contains(line.path("accountId").asLong());
            Map<Long, Long> balanceByAccount = JournalBalanceAggregator.signedBalancesThrough(
                    journalEntries,
                    asOf,
                    lineFilter
            );
            for (JsonNode a : accountsList) {
                long id = a.path("accountId").asLong();
                if (!shouldInclude(a, params)) {
                    continue;
                }
                long bal = balanceByAccount.getOrDefault(id, 0L);
                sumBalancesMinor += bal;
                rows.add(new String[]{text(a, "accountDisplayName"), ReportCurrencyFormat.formatMinorUnits(bal)});
            }
        }

        String title = "Account Balance Report";
        String subtitle = asOf == null
                ? "Current balances"
                : "Balances as of " + SUBTITLE_DATE.format(asOf);

        List<String> filterLines = ReportFilterSummary.buildLines(params, ledger, token);
        List<String> summaryLines = new ArrayList<>();
        summaryLines.add("Accounts listed: " + rows.size());
        summaryLines.add("Combined balance: " + ReportCurrencyFormat.formatMinorUnits(sumBalancesMinor));

        TableBlock table = new TableBlock(
                null,
                new String[]{"Account", "Balance"},
                rows,
                List.of(),
                Set.of(1),
                COL_WIDTHS,
                List.of(),
                false,
                Set.of()
        );

        return ReportPdfLayout.build(
                title,
                subtitle,
                ZonedDateTime.now(),
                filterLines,
                summaryLines,
                List.of(table),
                "No accounts matched these filters."
        );
    }

    private static boolean shouldInclude(JsonNode account, JsonNode params) {
        if (params.has("accountIds") && params.get("accountIds").isArray() && !params.get("accountIds").isEmpty()) {
            long id = account.path("accountId").asLong();
            boolean found = false;
            for (JsonNode n : params.get("accountIds")) {
                if (n.asLong() == id) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        if (params.has("accountTypeIds") && params.get("accountTypeIds").isArray() && !params.get("accountTypeIds").isEmpty()) {
            long typeId = account.path("typeId").asLong();
            boolean ok = false;
            for (JsonNode n : params.get("accountTypeIds")) {
                if (n.asLong() == typeId) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText();
    }

    private ObjectNode buildJournalFilter(LocalDate asOf, JsonNode parameters) {
        ObjectNode filtersWrapped = objectMapper.createObjectNode();
        filtersWrapped.set("dateAfter", objectMapper.getNodeFactory().nullNode());
        filtersWrapped.set("dateBefore", objectMapper.getNodeFactory().textNode(asOf.toString()));
        filtersWrapped.set("descriptionContains", objectMapper.getNodeFactory().nullNode());
        filtersWrapped.set("notesContains", objectMapper.getNodeFactory().nullNode());
        if (parameters.has("accountTypeIds") && parameters.get("accountTypeIds").isArray()
                && !parameters.get("accountTypeIds").isEmpty()) {
            filtersWrapped.set("accountTypes", parameters.get("accountTypeIds"));
        } else {
            filtersWrapped.set("accountTypes", objectMapper.getNodeFactory().nullNode());
        }
        if (parameters.has("accountIds") && parameters.get("accountIds").isArray() && !parameters.get("accountIds").isEmpty()) {
            filtersWrapped.set("accounts", parameters.get("accountIds"));
        } else {
            filtersWrapped.set("accounts", objectMapper.getNodeFactory().nullNode());
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.set("sort", objectMapper.getNodeFactory().nullNode());
        body.set("filters", filtersWrapped);
        return body;
    }
}
