package zachkingcade.dev.reporting.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Job metadata for JSON APIs. Do not use {@link com.fasterxml.jackson.databind.node.ObjectNode} in
 * {@link ApiResponse} payloads — Jackson serializes {@code JsonNode} as JavaBean introspection
 * instead of raw JSON (same issue as the catalog endpoint).
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ReportJobMetadataDto {

    private Long id;
    private String reportType;
    private String status;
    private Object requestParameters;
    private String requestedAt;
    private String startedAt;
    private String completedAt;
    private String failureReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Object requestParameters) {
        this.requestParameters = requestParameters;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
