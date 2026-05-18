package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bp_incomes")
@Getter
@Setter
public class BpIncomeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bp_income_id", nullable = false)
    private Long id;

    @Column(name = "budget_plan_id", nullable = false)
    private Long budgetPlanId;

    @Column(name = "amount_minor", nullable = false)
    private Long amountMinor;

    @Column(name = "frequency_unit", nullable = false)
    private String frequencyUnit;

    @Column(name = "frequency_number", nullable = false)
    private Integer frequencyNumber;

    @Column(name = "account_id", nullable = false)
    private Long accountId;
}
