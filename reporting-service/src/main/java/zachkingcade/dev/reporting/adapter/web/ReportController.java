package zachkingcade.dev.reporting.adapter.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import zachkingcade.dev.reporting.adapter.persistence.jpa.ReportJobEntity;
import zachkingcade.dev.reporting.adapter.web.dto.ApiResponse;
import zachkingcade.dev.reporting.adapter.web.dto.MetaData;
import zachkingcade.dev.reporting.adapter.web.dto.ReportJobMetadataDto;
import zachkingcade.dev.reporting.adapter.web.dto.ReportRequestDto;
import zachkingcade.dev.reporting.adapter.web.dto.catalog.CatalogReportDto;
import zachkingcade.dev.reporting.application.ReportingApplicationService;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportingApplicationService reportingApplicationService;
    private final ObjectMapper objectMapper;

    public ReportController(ReportingApplicationService reportingApplicationService, ObjectMapper objectMapper) {
        this.reportingApplicationService = reportingApplicationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<List<CatalogReportDto>>> catalog() {
        JsonNode tree = reportingApplicationService.catalog();
        if (!tree.isArray()) {
            throw new IllegalStateException("Report catalog must be a JSON array");
        }
        /*
         * Do not return JsonNode in ApiResponse#data: Jackson serializes JsonNode as a JavaBean
         * (isArray, containerNode, …) instead of raw JSON. Convert to typed POJOs for correct JSON.
         */
        List<CatalogReportDto> data = objectMapper.convertValue(tree, new TypeReference<List<CatalogReportDto>>() {});
        data.sort(Comparator.comparing(d -> d.getCode() != null ? d.getCode().toUpperCase(Locale.ROOT) : ""));
        return ResponseEntity.ok(new ApiResponse<>("Report catalog", new MetaData((long) data.size()), data));
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<ReportJobMetadataDto>> requestReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportRequestDto body
    ) {
        long userId = ReportingJwtUserIdExtractor.userId(jwt);
        String reportType = body.getReportType();
        JsonNode parameters = body.getParameters() == null
                ? objectMapper.createObjectNode()
                : objectMapper.valueToTree(body.getParameters());
        if (reportType == null || reportType.isBlank()) {
            throw new IllegalArgumentException("reportType is required");
        }
        ReportJobEntity job = reportingApplicationService.requestReport(userId, reportType, parameters);
        ReportJobMetadataDto data = toMetadataDto(job);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse<>("Report queued", new MetaData(1L), data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportJobMetadataDto>>> list(@AuthenticationPrincipal Jwt jwt) {
        long userId = ReportingJwtUserIdExtractor.userId(jwt);
        List<ReportJobMetadataDto> list = reportingApplicationService.listJobsForUser(userId).stream()
                .map(this::toMetadataDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("Reports for user", new MetaData((long) list.size()), list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportJobMetadataDto>> getOne(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        long userId = ReportingJwtUserIdExtractor.userId(jwt);
        ReportJobEntity job = reportingApplicationService.getJobForUser(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        return ResponseEntity.ok(new ApiResponse<>("Report metadata", new MetaData(1L), toMetadataDto(job)));
    }

    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        long userId = ReportingJwtUserIdExtractor.userId(jwt);
        var pdf = reportingApplicationService.getCompletedPdf(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report is not available for download"));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.getPdfContent());
    }

    private ReportJobMetadataDto toMetadataDto(ReportJobEntity job) {
        ReportJobMetadataDto d = new ReportJobMetadataDto();
        d.setId(job.getId());
        d.setReportType(job.getReportType());
        d.setStatus(job.getStatus().name());
        try {
            d.setRequestParameters(objectMapper.readValue(job.getRequestParameters(), Object.class));
        } catch (Exception e) {
            d.setRequestParameters(job.getRequestParameters());
        }
        d.setRequestedAt(job.getRequestedAt().toString());
        d.setStartedAt(job.getStartedAt() != null ? job.getStartedAt().toString() : null);
        d.setCompletedAt(job.getCompletedAt() != null ? job.getCompletedAt().toString() : null);
        d.setFailureReason(job.getFailureReason());
        return d;
    }
}
