package zachkingcade.dev.reporting.adapter.web.dto.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogReportDto {
    private String code;
    private String displayName;
    private String description;
    private List<CatalogFieldDto> fields = new ArrayList<>();

    public CatalogReportDto() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CatalogFieldDto> getFields() {
        return fields;
    }

    public void setFields(List<CatalogFieldDto> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }
}
