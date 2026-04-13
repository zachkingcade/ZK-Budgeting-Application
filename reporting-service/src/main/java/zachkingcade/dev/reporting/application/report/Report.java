package zachkingcade.dev.reporting.application.report;

import com.fasterxml.jackson.databind.JsonNode;

public interface Report {

    String code();

    com.fasterxml.jackson.databind.node.ObjectNode catalogJson();

    void validateParameters(JsonNode parameters);

    byte[] generatePdf(ReportContext context) throws Exception;
}
