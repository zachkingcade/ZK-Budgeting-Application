package zachkingcade.dev.reporting.adapter.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "completed_reports")
public class CompletedReportEntity {

    @Id
    @Column(name = "report_job_id")
    @Getter
    @Setter
    private Long reportJobId;

    /**
     * Store PDF bytes in {@code BYTEA}. Do not use {@code @Lob}: with PostgreSQL that maps to OID/BLOB
     * handling and Hibernate can bind a bigint instead of raw bytes (HHH000247 / 42804).
     */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "pdf_content", nullable = false, columnDefinition = "BYTEA")
    @Getter
    @Setter
    private byte[] pdfContent;

    @Column(name = "content_type", nullable = false)
    @Getter
    @Setter
    private String contentType;

    @Column(name = "stored_at", nullable = false)
    @Getter
    @Setter
    private Instant storedAt;
}
