package zachkingcade.dev.reporting.application.report;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Shared PDF chrome: title block, metadata, summary, styled tables (headers, borders, alternating rows).
 */
public final class ReportPdfLayout {

    private static final Color HEADER_BG = new Color(0xE8, 0xE8, 0xE8);
    private static final Color STRIPE_BG = new Color(0xF6, 0xF8, 0xFA);
    private static final Color BORDER_GRAY = new Color(0xC0, 0xC0, 0xC0);
    private static final float CELL_PADDING = 6f;
    private static final float BORDER_WIDTH = 0.4f;

    private static final DateTimeFormatter GENERATED_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z", Locale.US);

    private ReportPdfLayout() {
    }

    public record TableBlock(
            String sectionTitle,
            String[] headers,
            List<String[]> bodyRows,
            List<String[]> footerRows,
            Set<Integer> rightAlignedColumnIndexes,
            float[] columnWidths,
            /** Same length as {@code bodyRows}; non-blank entries render a full-width notes row below that body row. */
            List<String> bodyRowNotesBelow,
            /** When true, the section starts on a new PDF page (when not the first section). */
            boolean startOnNewPage,
            /**
             * When non-empty, the last footer row uses these indexes for right alignment; other footer rows use
             * {@code rightAlignedColumnIndexes}. When empty, all footer rows use {@code rightAlignedColumnIndexes}.
             */
            Set<Integer> footerLastRowRightAlignedColumnIndexes
    ) {
        public TableBlock {
            if (headers == null) {
                headers = new String[0];
            }
            if (bodyRows == null) {
                bodyRows = List.of();
            }
            if (footerRows == null) {
                footerRows = List.of();
            }
            if (rightAlignedColumnIndexes == null) {
                rightAlignedColumnIndexes = Set.of();
            }
            if (bodyRowNotesBelow == null) {
                bodyRowNotesBelow = List.of();
            }
            if (footerLastRowRightAlignedColumnIndexes == null) {
                footerLastRowRightAlignedColumnIndexes = Set.of();
            }
        }
    }

    public static byte[] build(
            String title,
            String subtitle,
            ZonedDateTime generatedAt,
            List<String> filterLines,
            List<String> summaryLines,
            List<TableBlock> tables,
            String emptyMessage
    ) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10f);
        subtitleFont.setColor(Color.DARK_GRAY);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9f);
        metaFont.setColor(Color.GRAY);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f);

        document.add(new Paragraph(title, titleFont));
        if (subtitle != null && !subtitle.isBlank()) {
            document.add(new Paragraph(subtitle, subtitleFont));
        }
        document.add(new Paragraph("Generated: " + GENERATED_FMT.format(generatedAt), metaFont));
        document.add(Chunk.NEWLINE);

        if (filterLines != null && !filterLines.isEmpty()) {
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f);
            document.add(new Paragraph("Filters applied", labelFont));
            for (String line : filterLines) {
                document.add(new Paragraph(line, bodyFont));
            }
            document.add(Chunk.NEWLINE);
        }

        if (summaryLines != null && !summaryLines.isEmpty()) {
            Font sumTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f);
            document.add(new Paragraph("Summary", sumTitle));
            PdfPTable sumTable = new PdfPTable(2);
            sumTable.setWidthPercentage(55f);
            sumTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            sumTable.setSpacingAfter(10f);
            for (String line : summaryLines) {
                int sep = line.indexOf(':');
                if (sep > 0 && sep < line.length() - 1) {
                    addKeyValueRow(sumTable, line.substring(0, sep).trim(), line.substring(sep + 1).trim(), bodyFont);
                } else {
                    PdfPCell c = bodyCell(line, 2, Element.ALIGN_LEFT, false, bodyFont);
                    c.setColspan(2);
                    sumTable.addCell(c);
                }
            }
            document.add(sumTable);
        }

        if (tables == null || tables.isEmpty() || allBodiesEmpty(tables)) {
            document.add(new Paragraph(emptyMessage != null ? emptyMessage : "No data was found for this report.", bodyFont));
        } else {
            boolean first = true;
            for (TableBlock block : tables) {
                if (!first && block.startOnNewPage()) {
                    document.newPage();
                } else if (!first) {
                    document.add(Chunk.NEWLINE);
                    document.add(Chunk.NEWLINE);
                }
                first = false;
                if (block.sectionTitle != null && !block.sectionTitle.isBlank()) {
                    Font secFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f);
                    document.add(new Paragraph(block.sectionTitle, secFont));
                    document.add(Chunk.NEWLINE);
                }
                document.add(buildStyledTable(block, bodyFont));
            }
        }

        document.close();
        return out.toByteArray();
    }

    private static boolean allBodiesEmpty(List<TableBlock> tables) {
        for (TableBlock b : tables) {
            if (!b.bodyRows.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void addKeyValueRow(PdfPTable table, String key, String value, Font font) {
        PdfPCell k = bodyCell(key, 1, Element.ALIGN_LEFT, false, font);
        styleSummaryCell(k, false);
        table.addCell(k);
        PdfPCell v = bodyCell(value, 1, Element.ALIGN_LEFT, false, font);
        styleSummaryCell(v, false);
        table.addCell(v);
    }

    private static void styleSummaryCell(PdfPCell cell, boolean header) {
        cell.setPadding(CELL_PADDING - 1f);
        cell.setBorderWidth(BORDER_WIDTH);
        cell.setBorderColor(BORDER_GRAY);
        if (header) {
            cell.setBackgroundColor(HEADER_BG);
        }
    }

    private static PdfPTable buildStyledTable(TableBlock block, Font bodyFont) throws DocumentException {
        int cols = block.headers.length;
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100f);
        if (block.columnWidths != null && block.columnWidths.length == cols) {
            table.setWidths(block.columnWidths);
        }
        table.setSpacingBefore(2f);
        table.setSpacingAfter(4f);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f);
        for (int i = 0; i < cols; i++) {
            String h = block.headers[i];
            PdfPCell cell = bodyCell(h == null ? "" : h, 1, Element.ALIGN_LEFT, true, headerFont);
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(block.rightAlignedColumnIndexes.contains(i) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            cell.setBorderWidth(BORDER_WIDTH);
            cell.setBorderColor(BORDER_GRAY);
            cell.setPadding(CELL_PADDING);
            table.addCell(cell);
        }

        List<String> notesBelow = block.bodyRowNotesBelow();
        int bodyRowIndex = 0;
        for (int r = 0; r < block.bodyRows.size(); r++) {
            String[] row = block.bodyRows.get(r);
            boolean stripe = bodyRowIndex % 2 == 1;
            for (int i = 0; i < cols; i++) {
                String v = i < row.length && row[i] != null ? row[i] : "";
                boolean right = block.rightAlignedColumnIndexes.contains(i);
                PdfPCell cell = bodyCell(v, 1, right ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT, false, bodyFont);
                cell.setBorderWidth(BORDER_WIDTH);
                cell.setBorderColor(BORDER_GRAY);
                cell.setPadding(CELL_PADDING);
                if (stripe) {
                    cell.setBackgroundColor(STRIPE_BG);
                }
                table.addCell(cell);
            }
            String noteBelow = r < notesBelow.size() ? notesBelow.get(r) : "";
            if (noteBelow != null && !noteBelow.isBlank()) {
                Font noteFont = FontFactory.getFont(FontFactory.HELVETICA, 9f);
                noteFont.setColor(Color.DARK_GRAY);
                PdfPCell noteCell = bodyCell(noteBelow, cols, Element.ALIGN_LEFT, false, noteFont);
                noteCell.setBorderWidth(BORDER_WIDTH);
                noteCell.setBorderColor(BORDER_GRAY);
                noteCell.setPadding(CELL_PADDING - 1f);
                noteCell.setPaddingLeft(CELL_PADDING + 8f);
                if (stripe) {
                    noteCell.setBackgroundColor(STRIPE_BG);
                }
                table.addCell(noteCell);
            }
            bodyRowIndex++;
        }

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f);
        List<String[]> footerRows = block.footerRows();
        for (int fr = 0; fr < footerRows.size(); fr++) {
            String[] row = footerRows.get(fr);
            boolean lastFooterRow = fr == footerRows.size() - 1;
            Set<Integer> footerLastRight = block.footerLastRowRightAlignedColumnIndexes();
            boolean useFooterLastRight = lastFooterRow && !footerLastRight.isEmpty();
            for (int i = 0; i < cols; i++) {
                String v = i < row.length && row[i] != null ? row[i] : "";
                boolean right = useFooterLastRight
                        ? footerLastRight.contains(i)
                        : block.rightAlignedColumnIndexes.contains(i);
                PdfPCell cell = bodyCell(v, 1, right ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT, false, footerFont);
                cell.setBorderWidth(BORDER_WIDTH);
                cell.setBorderColor(BORDER_GRAY);
                cell.setPadding(CELL_PADDING);
                cell.setBackgroundColor(HEADER_BG);
                table.addCell(cell);
            }
        }

        return table;
    }

    private static PdfPCell bodyCell(String text, int colspan, int align, boolean header, Font font) {
        Phrase phrase = new Phrase(text == null ? "" : text, font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    /**
     * Single simple table with legacy-style call sites (title only in document — prefer {@link #build} for new reports).
     */
    public static byte[] buildSimpleTitleAndTable(
            String title,
            List<String[]> tableRows,
            Set<Integer> rightAlignedColumnIndexes,
            String... header
    ) throws DocumentException {
        List<TableBlock> blocks = new ArrayList<>();
        if (header.length > 0 && !tableRows.isEmpty()) {
            float[] widths = new float[header.length];
            for (int i = 0; i < widths.length; i++) {
                widths[i] = 1f;
            }
            blocks.add(new TableBlock(
                    null,
                    header,
                    tableRows,
                    List.of(),
                    rightAlignedColumnIndexes,
                    widths,
                    List.of(),
                    false,
                    Set.of()
            ));
        }
        return build(
                title,
                "",
                ZonedDateTime.now(),
                List.of(),
                List.of(),
                blocks,
                "No data was found for this report."
        );
    }
}
