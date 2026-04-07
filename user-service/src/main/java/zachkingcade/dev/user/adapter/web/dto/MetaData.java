package zachkingcade.dev.user.adapter.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class MetaData {
    private final LocalDate requestDate;
    private final LocalTime requestTime;
    private Long executionTimeMs;
    private Long dataResponseCount;

    public MetaData() {
        this.requestDate = LocalDate.now();
        this.requestTime = LocalTime.now();
    }

    public MetaData(Long dataResponseCount) {
        this.requestDate = LocalDate.now();
        this.requestTime = LocalTime.now();
        this.dataResponseCount = dataResponseCount;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public LocalTime getRequestTime() {
        return requestTime;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Long getDataResponseCount() {
        return dataResponseCount;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public void setDataResponseCount(Long dataResponseCount) {
        this.dataResponseCount = dataResponseCount;
    }
}
