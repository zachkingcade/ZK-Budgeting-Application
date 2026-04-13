package zachkingcade.dev.reporting.application.report;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReportRegistry {

    private final Map<String, Report> byCode;

    public ReportRegistry(List<Report> reports) {
        this.byCode = reports.stream().collect(Collectors.toMap(
                r -> r.code().toUpperCase(Locale.ROOT),
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException("Duplicate report code");
                }
        ));
    }

    public Report get(String reportType) {
        if (reportType == null) {
            return null;
        }
        return byCode.get(reportType.trim().toUpperCase(Locale.ROOT));
    }

    public com.fasterxml.jackson.databind.node.ArrayNode buildCatalog(com.fasterxml.jackson.databind.ObjectMapper mapper) {
        var arr = mapper.createArrayNode();
        for (Report r : byCode.values()) {
            arr.add(r.catalogJson());
        }
        return arr;
    }
}
