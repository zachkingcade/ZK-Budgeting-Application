package zachkingcade.dev.reporting.application.report;

import com.lowagie.text.DocumentException;

import java.util.List;
import java.util.Set;

public final class PdfBytes {

    private PdfBytes() {
    }

    public static byte[] build(String title, List<String[]> tableRows, String... header) throws DocumentException {
        return build(title, tableRows, Set.of(), header);
    }

    /**
     * @param rightAlignedColumnIndexes zero-based column indexes aligned to the right (e.g. currency columns)
     */
    public static byte[] build(
            String title,
            List<String[]> tableRows,
            Set<Integer> rightAlignedColumnIndexes,
            String... header
    ) throws DocumentException {
        return ReportPdfLayout.buildSimpleTitleAndTable(title, tableRows, rightAlignedColumnIndexes, header);
    }
}
