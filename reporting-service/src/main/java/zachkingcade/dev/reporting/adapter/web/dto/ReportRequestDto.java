package zachkingcade.dev.reporting.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Request body for {@code POST /reports/requests}. Do not use {@link com.fasterxml.jackson.databind.node.ObjectNode}
 * as the {@code @RequestBody} type: Jackson 3 cannot build a deserializer for {@code ObjectNode}
 * (conflicting {@code setAll} overloads for property {@code all}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportRequestDto {

    private String reportType;
    private Map<String, Object> parameters;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
