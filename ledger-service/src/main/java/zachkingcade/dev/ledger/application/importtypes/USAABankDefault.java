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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component("USAABankDefault")
public class USAABankDefault implements ImportType {

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

            List<PendingTransactionDraft> drafts = new ArrayList<>();
            for (int i = 1; i < records.size(); i++) {
                CSVRecord row = records.get(i);
                if (row == null || row.size() == 0) {
                    continue;
                }

                String rawDate = safeGet(row, dateIdx);
                String rawDesc = safeGet(row, descIdx);
                String rawAmount = safeGet(row, amountIdx);

                if ((rawDate == null || rawDate.isBlank()) && (rawDesc == null || rawDesc.isBlank()) && (rawAmount == null || rawAmount.isBlank())) {
                    continue;
                }

                LocalDate txDate = parseDateWithFallback(rawDate, i + 1, dateFormatter);

                String description = (rawDesc == null ? "" : rawDesc.trim());
                if (description.isBlank()) {
                    throw new ApplicationException(String.format("Missing Description on CSV row [%s].", i + 1));
                }

                Long amountMinor = parseAbsMinorUnits(rawAmount, i + 1);

                String notes = "";

                drafts.add(new PendingTransactionDraft(txDate, description, amountMinor, notes));
            }

            return drafts;
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Failed to parse CSV file.");
        }
    }

    private static LocalDate parseDateWithFallback(String rawDate, int csvRowNumber, DateTimeFormatter configuredFormatter) {
        String trimmed = rawDate == null ? "" : rawDate.trim();
        if (trimmed.isBlank()) {
            throw new ApplicationException(String.format("Missing Date on CSV row [%s].", csvRowNumber));
        }

        try {
            return LocalDate.parse(trimmed, configuredFormatter);
        } catch (DateTimeParseException ex) {
            // try ISO fallback below
        }

        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new ApplicationException(String.format("Invalid date [%s] on CSV row [%s].", rawDate, csvRowNumber));
        }
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

}

