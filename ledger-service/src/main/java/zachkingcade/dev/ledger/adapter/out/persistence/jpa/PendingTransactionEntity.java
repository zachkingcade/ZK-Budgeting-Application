package zachkingcade.dev.ledger.adapter.out.persistence.jpa;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "pending_transactions")
public class PendingTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_number", nullable = false)
    private Long transactionNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_date", nullable = false)
    private Date transactionDate;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "notes", nullable = false)
    private String notes = "";

    public Long getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(Long transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

