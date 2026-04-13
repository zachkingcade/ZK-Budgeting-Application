package zachkingcade.dev.reporting.adapter.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import zachkingcade.dev.reporting.domain.report.ReportJobStatus;

import java.time.Instant;

@Entity
@Table(name = "report_jobs")
public class ReportJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(name = "owning_user_id", nullable = false)
    @Getter
    @Setter
    private Long owningUserId;

    @Column(name = "report_type", nullable = false)
    @Getter
    @Setter
    private String reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Getter
    @Setter
    private ReportJobStatus status;

    @Column(name = "request_parameters", nullable = false, length = 65535)
    @Getter
    @Setter
    private String requestParameters;

    @Column(name = "requested_at", nullable = false)
    @Getter
    @Setter
    private Instant requestedAt;

    @Column(name = "started_at")
    @Getter
    @Setter
    private Instant startedAt;

    @Column(name = "completed_at")
    @Getter
    @Setter
    private Instant completedAt;

    @Column(name = "failure_reason")
    @Getter
    @Setter
    private String failureReason;

    @Version
    @Column(name = "version", nullable = false)
    @Getter
    @Setter
    private Long version;
}
