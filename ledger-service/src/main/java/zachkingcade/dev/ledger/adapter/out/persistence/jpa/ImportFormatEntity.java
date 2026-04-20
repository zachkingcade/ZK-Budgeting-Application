package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "import_formats")
public class ImportFormatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "format_id")
    private Long id;

    @Column(name = "format_name", nullable = false)
    private String formatName;

    @Column(name = "format_type", nullable = false)
    private String formatType;

    @Column(name = "format_details", nullable = false, columnDefinition = "jsonb")
    private String formatDetails;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "bean_name", nullable = false)
    private String beanName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String getFormatDetails() {
        return formatDetails;
    }

    public void setFormatDetails(String formatDetails) {
        this.formatDetails = formatDetails;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}

