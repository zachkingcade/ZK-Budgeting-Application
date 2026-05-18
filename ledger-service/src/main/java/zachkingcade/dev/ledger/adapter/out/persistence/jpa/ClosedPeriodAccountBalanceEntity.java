package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "closed_period_account_balances")
@Getter
@Setter
public class ClosedPeriodAccountBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closed_period_account_balance_id", nullable = false)
    private Long closedPeriodAccountBalanceId;

    @Column(name = "closed_accounting_period_id", nullable = false)
    private Long closedAccountingPeriodId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "balance", nullable = false)
    private Long balance;
}
