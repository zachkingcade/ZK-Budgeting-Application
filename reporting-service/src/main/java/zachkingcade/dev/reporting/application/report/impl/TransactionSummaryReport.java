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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TransactionSummaryReport implements Report {

    public static final String CODE = "TRANSACTION_SUMMARY";

    private static final DateTimeFormatter SUBTITLE_DATE =
            DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM).withLocale(Locale.US);

    private static final String[] COL_HEADERS = {
            "Date",
            "Description",
            "Entry notes",
            "Debit",
            "Credit",
            "Effect"
    };

    private static final float[] COL_WIDTHS = {10f, 22f, 18f, 12f, 12f, 8f};

    /** Main body: debit/credit columns. */
    private static final Set<Integer> RIGHT_ALIGN = Set.of(3, 4);

    /** Last footer row: right-align currency cells. */
    private static final Set<Integer> FOOTER_LAST_ROW_RIGHT = Set.of(0, 2, 3, 4, 5);

    private final ObjectMapper objectMapper;

    public TransactionSummaryReport(ObjectMapper objectMapper) {
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
        root.put("displayName", "Transaction Summary Report");
        root.put("description", "Lists journal activity in a period, grouped by account.");
        var fields = objectMapper.createArrayNode();
        fields.add(field("dateFrom", "Start date (inclusive)", "date", "Period start.", true));
        fields.add(field("dateTo", "End date (inclusive)", "date", "Period end.", true));
        fields.add(field("includeJournalLines", "Include journal lines", "checkbox",
                "When checked, each journal line is listed. When unchecked, one row per journal entry per account.", false));
        fields.add(field("newPagePerAccount", "Start each account on a new page", "checkbox",
                "When checked, each account section begins on a new page.", false));
        fields.add(field("accountIds", "Accounts (optional)", "account-multi", "If omitted, includes all accounts.", false));
        fields.add(field("accountTypeIds", "Account types (optional)", "account-type-multi", "If omitted, includes all account types.", false));
        root.set("fields", fields);
        return root;
    }

    private ObjectNode field(String name, String description, String type, String extra, boolean required) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("description", description + " " + extra);
        node.put("type", type);
        node.put("required", required);
        return node;
    }

    @Override
    public void validateParameters(JsonNode parameters) {
        if (parameters == null || !parameters.hasNonNull("dateFrom") || !parameters.hasNonNull("dateTo")) {
            throw new IllegalArgumentException("dateFrom and dateTo are required");
        }
        LocalDate from = LocalDate.parse(parameters.get("dateFrom").asText());
        LocalDate to = LocalDate.parse(parameters.get("dateTo").asText());
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("dateTo must be on or after dateFrom");
        }
    }

    @Override
    public byte[] generatePdf(ReportContext context) throws Exception {
        JsonNode parameters = context.parameters();
        LocalDate from = LocalDate.parse(parameters.get("dateFrom").asText());
        LocalDate to = LocalDate.parse(parameters.get("dateTo").asText());
        boolean includeJournalLines = parameters.path("includeJournalLines").asBoolean(false);
        boolean newPagePerAccount = parameters.path("newPagePerAccount").asBoolean(false);
        String token = context.ledgerToken();
        LedgerApiClient ledger = context.ledgerApiClient();

        Map<Long, Long> accountIdToTypeId = ReportFilterSummary.accountIdToTypeId(ledger, token);
        Set<Long> accountIdFilter = readIdSet(parameters, "accountIds");
        Set<Long> accountTypeIdFilter = readIdSet(parameters, "accountTypeIds");
        Predicate<JsonNode> linePredicate =
                JournalBalanceAggregator.lineMatchesParameterFilter(accountIdFilter, accountTypeIdFilter, accountIdToTypeId);

        JsonNode openingJournal = ledger.postJournalFiltered(token, buildJournalBody(null, from.minusDays(1), parameters));
        Map<Long, Long> openingSigned = JournalBalanceAggregator.signedBalancesThrough(
                openingJournal.path("journalEntryList"),
                from.minusDays(1),
                linePredicate
        );

        JsonNode periodJournal = ledger.postJournalFiltered(token, buildJournalBody(from, to, parameters));
        JsonNode journalEntries = periodJournal.path("journalEntryList");

        List<LineRow> rawLines = collectLines(journalEntries, linePredicate);

        Map<Long, List<LineRow>> byAccount = rawLines.stream()
                .collect(Collectors.groupingBy(l -> l.accountId, HashMap::new, Collectors.toCollection(ArrayList::new)));
        for (List<LineRow> list : byAccount.values()) {
            list.sort(Comparator
                    .comparing((LineRow r) -> LocalDate.parse(r.entryDate))
                    .thenComparingLong(r -> r.entryId)
                    .thenComparingLong(r -> r.lineId));
        }

        List<Long> accountOrder = new ArrayList<>(byAccount.keySet());
        accountOrder.sort(Comparator
                .comparing((Long accountId) -> byAccount.get(accountId).get(0).accountDisplayName)
                .thenComparingLong(accountId -> accountId));

        long totalDebitsMinor = 0L;
        long totalCreditsMinor = 0L;
        Set<Long> distinctEntries = new HashSet<>();
        for (LineRow lr : rawLines) {
            distinctEntries.add(lr.entryId);
            if (lr.direction == 'D') {
                totalDebitsMinor += lr.amountMinor;
            } else {
                totalCreditsMinor += lr.amountMinor;
            }
        }

        Map<Long, Long> signedPeriodByAccount =
                JournalBalanceAggregator.signedBalancesForLinesInEntries(journalEntries, linePredicate);

        Map<Long, List<LineRow>> displayByAccount = new HashMap<>();
        for (Long aid : accountOrder) {
            List<LineRow> accountLines = byAccount.get(aid);
            displayByAccount.put(aid, includeJournalLines ? accountLines : collapseToEntryRows(accountLines));
        }
        long rowsDisplayed = displayByAccount.values().stream().mapToLong(List::size).sum();

        List<TableBlock> blocks = new ArrayList<>();
        for (int idx = 0; idx < accountOrder.size(); idx++) {
            Long accountId = accountOrder.get(idx);
            List<LineRow> accountLines = byAccount.get(accountId);
            LineRow sample = accountLines.get(0);
            String sectionTitle = sample.accountDisplayName
                    + (sample.accountTypeName.isBlank() ? "" : " — " + sample.accountTypeName);

            List<LineRow> displaySource = displayByAccount.get(accountId);

            List<String[]> body = new ArrayList<>();
            List<String> notesBelow = new ArrayList<>();
            long periodDebitSum = 0L;
            long periodCreditSum = 0L;

            for (LineRow r : displaySource) {
                periodDebitSum += r.debitSumMinor;
                periodCreditSum += r.creditSumMinor;
                String debit = r.debitSumMinor > 0 ? ReportCurrencyFormat.formatMinorUnits(r.debitSumMinor) : "";
                String credit = r.creditSumMinor > 0 ? ReportCurrencyFormat.formatMinorUnits(r.creditSumMinor) : "";
                body.add(new String[]{
                        SUBTITLE_DATE.format(LocalDate.parse(r.entryDate)),
                        r.entryDescription,
                        r.entryNotes.isBlank() ? "—" : r.entryNotes,
                        debit,
                        credit,
                        r.effectShort
                });
                notesBelow.add(r.lineNotes.isBlank() ? "" : r.lineNotes);
            }

            long opening = openingSigned.getOrDefault(accountId, 0L);
            long signedNet = signedPeriodByAccount.getOrDefault(accountId, 0L);
            long closing = opening + signedNet;

            List<String[]> footer = new ArrayList<>();
            footer.add(new String[]{
                    "Net change",
                    "",
                    "Opening",
                    "Period debits",
                    "Period credits",
                    "Closing"
            });
            footer.add(new String[]{
                    ReportCurrencyFormat.formatMinorUnits(signedNet),
                    "",
                    ReportCurrencyFormat.formatMinorUnits(opening),
                    ReportCurrencyFormat.formatMinorUnits(periodDebitSum),
                    ReportCurrencyFormat.formatMinorUnits(periodCreditSum),
                    ReportCurrencyFormat.formatMinorUnits(closing)
            });

            blocks.add(new TableBlock(
                    sectionTitle,
                    COL_HEADERS,
                    body,
                    footer,
                    RIGHT_ALIGN,
                    COL_WIDTHS,
                    notesBelow,
                    newPagePerAccount && idx > 0,
                    FOOTER_LAST_ROW_RIGHT
            ));
        }

        String title = "Transaction Summary";
        String subtitle = SUBTITLE_DATE.format(from) + " – " + SUBTITLE_DATE.format(to);
        List<String> filterLines = ReportFilterSummary.buildLines(parameters, ledger, token);
        List<String> summaryLines = new ArrayList<>();
        summaryLines.add("Journal entries in range: " + distinctEntries.size());
        if (includeJournalLines) {
            summaryLines.add("Journal lines included: " + rawLines.size());
        } else {
            summaryLines.add("Rows displayed (entry-level): " + rowsDisplayed);
        }
        summaryLines.add("Accounts with activity: " + byAccount.size());
        summaryLines.add("Total debits (lines): " + ReportCurrencyFormat.formatMinorUnits(totalDebitsMinor));
        summaryLines.add("Total credits (lines): " + ReportCurrencyFormat.formatMinorUnits(totalCreditsMinor));

        return ReportPdfLayout.build(
                title,
                subtitle,
                ZonedDateTime.now(),
                filterLines,
                summaryLines,
                blocks,
                "No journal lines matched these filters for the selected period."
        );
    }

    private List<LineRow> collectLines(JsonNode journalEntries, Predicate<JsonNode> linePredicate) {
        List<LineRow> lines = new ArrayList<>();
        if (!journalEntries.isArray()) {
            return lines;
        }
        for (JsonNode entry : journalEntries) {
            long entryId = entry.path("id").asLong(0L);
            String entryDate = entry.path("entryDate").asText("");
            String entryDesc = entry.path("description").asText("");
            String entryNotes = entry.path("notes").asText("");
            JsonNode journalLines = entry.path("journalLines");
            if (!journalLines.isArray()) {
                continue;
            }
            for (JsonNode line : journalLines) {
                if (!linePredicate.test(line)) {
                    continue;
                }
                long accountId = line.path("accountId").asLong();
                String display = line.path("accountDisplayName").asText("?");
                String typeName = line.path("accountTypeName").asText("");
                long lineId = line.path("id").asLong(0L);
                long amount = line.path("amount").asLong();
                String dirStr = line.path("direction").asText("D");
                char dch = (!dirStr.isEmpty() && dirStr.charAt(0) == 'C') ? 'C' : 'D';
                long debitPart = dch == 'D' ? amount : 0L;
                long creditPart = dch == 'C' ? amount : 0L;
                lines.add(new LineRow(
                        accountId,
                        display,
                        typeName,
                        entryDate,
                        entryId,
                        lineId,
                        entryDesc,
                        entryNotes,
                        amount,
                        dch,
                        effectShort(line),
                        line.path("notes").asText(""),
                        debitPart,
                        creditPart,
                        JournalBalanceAggregator.signedAmountForLine(line)
                ));
            }
        }
        return lines;
    }

    /**
     * One row per (account, journal entry), with debits/credits summed for that account on the entry.
     */
    private static List<LineRow> collapseToEntryRows(List<LineRow> accountLines) {
        Map<String, List<LineRow>> byEntry = new HashMap<>();
        for (LineRow lr : accountLines) {
            byEntry.computeIfAbsent(entryKey(lr.accountId, lr.entryId), k -> new ArrayList<>()).add(lr);
        }
        List<String> keys = new ArrayList<>(byEntry.keySet());
        keys.sort(Comparator
                .comparing((String k) -> LocalDate.parse(byEntry.get(k).get(0).entryDate))
                .thenComparingLong(k -> byEntry.get(k).get(0).entryId));

        List<LineRow> out = new ArrayList<>();
        for (String k : keys) {
            List<LineRow> ls = byEntry.get(k);
            LineRow first = ls.get(0);
            long debitSum = 0L;
            long creditSum = 0L;
            long signedSum = 0L;
            StringJoiner noteJoin = new StringJoiner("; ");
            for (LineRow lr : ls) {
                debitSum += lr.debitSumMinor;
                creditSum += lr.creditSumMinor;
                signedSum += lr.signedMinor;
                if (!lr.lineNotes.isBlank()) {
                    noteJoin.add(lr.lineNotes);
                }
            }
            String joinedNotes = noteJoin.toString();
            String effect = signedSum >= 0 ? "(+)" : "(-)";
            out.add(new LineRow(
                    first.accountId,
                    first.accountDisplayName,
                    first.accountTypeName,
                    first.entryDate,
                    first.entryId,
                    0L,
                    first.entryDescription,
                    first.entryNotes,
                    0L,
                    'D',
                    effect,
                    joinedNotes,
                    debitSum,
                    creditSum,
                    signedSum
            ));
        }
        return out;
    }

    private static String entryKey(long accountId, long entryId) {
        return accountId + ":" + entryId;
    }

    private static String effectShort(JsonNode line) {
        String aff = line.path("lineAffectOnAccount").asText("+");
        return "+".equals(aff) ? "(+)" : "(-)";
    }

    private ObjectNode buildJournalBody(LocalDate dateAfterInclusive, LocalDate dateBeforeInclusive, JsonNode parameters) {
        ObjectNode filters = objectMapper.createObjectNode();
        if (dateAfterInclusive != null) {
            filters.put("dateAfter", dateAfterInclusive.toString());
        } else {
            filters.set("dateAfter", objectMapper.getNodeFactory().nullNode());
        }
        filters.put("dateBefore", dateBeforeInclusive.toString());
        filters.set("descriptionContains", objectMapper.getNodeFactory().nullNode());
        filters.set("notesContains", objectMapper.getNodeFactory().nullNode());

        if (parameters.has("accountTypeIds") && parameters.get("accountTypeIds").isArray()
                && !parameters.get("accountTypeIds").isEmpty()) {
            filters.set("accountTypes", parameters.get("accountTypeIds"));
        } else {
            filters.set("accountTypes", objectMapper.getNodeFactory().nullNode());
        }
        if (parameters.has("accountIds") && parameters.get("accountIds").isArray() && !parameters.get("accountIds").isEmpty()) {
            filters.set("accounts", parameters.get("accountIds"));
        } else {
            filters.set("accounts", objectMapper.getNodeFactory().nullNode());
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.set("sort", objectMapper.getNodeFactory().nullNode());
        body.set("filters", filters);
        return body;
    }

    private static Set<Long> readIdSet(JsonNode parameters, String field) {
        if (!parameters.has(field) || !parameters.get(field).isArray() || parameters.get(field).isEmpty()) {
            return Set.of();
        }
        Set<Long> out = new HashSet<>();
        for (JsonNode n : parameters.get(field)) {
            out.add(n.asLong());
        }
        return out;
    }

    /**
     * @param debitSumMinor creditSumMinor signedMinor used for entry-level rows; for journal-line rows,
     *                     debitSumMinor/creditSumMinor mirror the single line direction amounts.
     */
    private record LineRow(
            long accountId,
            String accountDisplayName,
            String accountTypeName,
            String entryDate,
            long entryId,
            long lineId,
            String entryDescription,
            String entryNotes,
            long amountMinor,
            char direction,
            String effectShort,
            String lineNotes,
            long debitSumMinor,
            long creditSumMinor,
            long signedMinor
    ) {
    }
}
