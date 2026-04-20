package zachkingcade.dev.ledger.application.importtypes;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import zachkingcade.dev.ledger.application.exception.ApplicationException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("USAABankDefault")
public class USAABankDefault implements ImportType {

    private static final Path DEBUG_LOG_PATH = Path.of("/home/zacharykingcade/Documents/code_projects/ZK-Budgeting-Application/.cursor/debug-ee0c2b.log");

    @Override
    public List<PendingTransactionDraft> parse(InputStream inputStream, ImportFormatDetails details) {
        if (details == null || details.headerArray() == null || details.headerArray().isEmpty()) {
            throw new ApplicationException("Import format details missing headerArray.");
        }
        if (details.dateFormat() == null || details.dateFormat().isBlank()) {
            throw new ApplicationException("Import format details missing dateFormat.");
        }

        DateTimeFormatter dateFormatter;
        try {
            dateFormatter = DateTimeFormatter.ofPattern(details.dateFormat());
        } catch (IllegalArgumentException ex) {
            throw new ApplicationException(String.format("Invalid dateFormat [%s] for import format.", details.dateFormat()));
        }

        try {
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                throw new ApplicationException("CSV contained no rows.");
            }

            CSVRecord headerRow = records.get(0);
            validateHeaderRow(headerRow, details.headerArray());

            int dateIdx = indexOf(details.headerArray(), "Date");
            int descIdx = indexOf(details.headerArray(), "Description");
            int amountIdx = indexOf(details.headerArray(), "Amount");
            int statusIdx = indexOf(details.headerArray(), "Status");

            List<PendingTransactionDraft> drafts = new ArrayList<>();
            for (int i = 1; i < records.size(); i++) {
                CSVRecord row = records.get(i);
                if (row == null || row.size() == 0) {
                    continue;
                }

                String rawDate = safeGet(row, dateIdx);
                String rawDesc = safeGet(row, descIdx);
                String rawAmount = safeGet(row, amountIdx);
                String rawStatus = safeGet(row, statusIdx);

                if ((rawDate == null || rawDate.isBlank()) && (rawDesc == null || rawDesc.isBlank()) && (rawAmount == null || rawAmount.isBlank())) {
                    continue;
                }

                LocalDate txDate = parseDateWithFallback(rawDate, i + 1, details.dateFormat(), dateFormatter);

                String description = (rawDesc == null ? "" : rawDesc.trim());
                if (description.isBlank()) {
                    throw new ApplicationException(String.format("Missing Description on CSV row [%s].", i + 1));
                }

                Long amountMinor = parseAbsMinorUnits(rawAmount, i + 1);

                String notes = "";
                if (rawStatus != null && rawStatus.trim().equalsIgnoreCase("pending")) {
                    notes = appendPending(notes);
                }

                drafts.add(new PendingTransactionDraft(txDate, description, amountMinor, notes));
            }

            return drafts;
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Failed to parse CSV file.");
        }
    }

    private static LocalDate parseDateWithFallback(String rawDate, int csvRowNumber, String configuredFormat, DateTimeFormatter configuredFormatter) {
        String trimmed = rawDate == null ? "" : rawDate.trim();
        if (trimmed.isBlank()) {
            throw new ApplicationException(String.format("Missing Date on CSV row [%s].", csvRowNumber));
        }

        try {
            LocalDate parsed = LocalDate.parse(trimmed, configuredFormatter);
            debugLog("Parsed date using configured format", Map.of(
                    "csvRowNumber", csvRowNumber,
                    "rawDate", trimmed,
                    "configuredFormat", configuredFormat,
                    "parsed", parsed.toString(),
                    "used", "configured"
            ));
            return parsed;
        } catch (DateTimeParseException ex) {
            debugLog("Configured date parse failed; trying ISO_LOCAL_DATE", Map.of(
                    "csvRowNumber", csvRowNumber,
                    "rawDate", trimmed,
                    "configuredFormat", configuredFormat,
                    "used", "configured_failed"
            ));
        }

        try {
            LocalDate parsed = LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
            debugLog("Parsed date using ISO_LOCAL_DATE fallback", Map.of(
                    "csvRowNumber", csvRowNumber,
                    "rawDate", trimmed,
                    "configuredFormat", configuredFormat,
                    "parsed", parsed.toString(),
                    "used", "iso_local_date"
            ));
            return parsed;
        } catch (DateTimeParseException ex) {
            debugLog("ISO_LOCAL_DATE fallback failed", Map.of(
                    "csvRowNumber", csvRowNumber,
                    "rawDate", trimmed,
                    "configuredFormat", configuredFormat,
                    "used", "iso_failed"
            ));
            throw new ApplicationException(String.format("Invalid date [%s] on CSV row [%s].", rawDate, csvRowNumber));
        }
    }

    private static void debugLog(String message, Map<String, Object> data) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("sessionId", "ee0c2b");
            payload.put("runId", "pre-fix");
            payload.put("hypothesisId", "H1");
            payload.put("location", "USAABankDefault.java");
            payload.put("message", message);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("data", data);

            String json = toJson(payload) + "\n";
            Files.writeString(DEBUG_LOG_PATH, json, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception _ignored) {
            // intentionally ignore debug logging failures
        }
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(e.getKey())).append("\":");
            sb.append(valueToJson(e.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map<?, ?> m) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) m;
            return toJson(cast);
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private static String escapeJson(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static int indexOf(List<String> headerArray, String value) {
        for (int i = 0; i < headerArray.size(); i++) {
            if (value.equals(headerArray.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void validateHeaderRow(CSVRecord headerRow, List<String> expectedHeader) {
        if (headerRow.size() != expectedHeader.size()) {
            throw new ApplicationException(String.format(
                    "CSV header length [%s] does not match expected [%s].",
                    headerRow.size(),
                    expectedHeader.size()
            ));
        }
        for (int i = 0; i < expectedHeader.size(); i++) {
            String got = headerRow.get(i);
            String expected = expectedHeader.get(i);
            if (!expected.equals(got)) {
                throw new ApplicationException(String.format(
                        "CSV header mismatch at index [%s]. Expected [%s] but got [%s].",
                        i,
                        expected,
                        got
                ));
            }
        }
    }

    private static String safeGet(CSVRecord row, int idx) {
        if (idx < 0 || idx >= row.size()) {
            return "";
        }
        return row.get(idx);
    }

    private static Long parseAbsMinorUnits(String rawAmount, int csvRowNumber) {
        if (rawAmount == null || rawAmount.isBlank()) {
            throw new ApplicationException(String.format("Missing Amount on CSV row [%s].", csvRowNumber));
        }
        try {
            String normalized = rawAmount
                    .trim()
                    .replace("$", "")
                    .replace(",", "");
            BigDecimal major = new BigDecimal(normalized);
            BigDecimal minor = major.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).abs();
            long value = minor.longValueExact();
            if (value < 1) {
                throw new ApplicationException(String.format("Amount must be non-zero on CSV row [%s].", csvRowNumber));
            }
            return value;
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(String.format("Invalid Amount [%s] on CSV row [%s].", rawAmount, csvRowNumber));
        }
    }

    private static String appendPending(String existingNotes) {
        String base = existingNotes == null ? "" : existingNotes.trim();
        if (base.isEmpty()) {
            return "PENDING";
        }
        if (base.toUpperCase().contains("PENDING")) {
            return base;
        }
        return base + " PENDING";
    }
}

