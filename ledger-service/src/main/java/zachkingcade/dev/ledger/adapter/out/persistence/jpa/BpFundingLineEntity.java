package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bp_funding_lines")
@Getter
@Setter
public class BpFundingLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bp_funding_line_id", nullable = false)
    private Long id;

    @Column(name = "bp_budget_id", nullable = false)
    private Long bpBudgetId;

    @Column(name = "bp_income_id", nullable = false)
    private Long bpIncomeId;

    @Column(name = "amount_minor", nullable = false)
    private Long amountMinor;

    @Column(name = "frequency_unit", nullable = false)
    private String frequencyUnit;

    @Column(name = "frequency_number", nullable = false)
    private Integer frequencyNumber;
}
