package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bp_budgets")
@Getter
@Setter
public class BpBudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bp_budget_id", nullable = false)
    private Long id;

    @Column(name = "budget_plan_id", nullable = false)
    private Long budgetPlanId;

    @Column(name = "target_amount_per_day_minor", nullable = false, precision = 30, scale = 12)
    private BigDecimal targetAmountPerDayMinor;

    @Column(name = "account_id", nullable = false)
    private Long accountId;
}
