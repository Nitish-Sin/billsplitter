package com.billsplitter.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_approvals",
       uniqueConstraints = @UniqueConstraint(columnNames = {"expense_id", "approver_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalAction action;

    @Column
    private String comment;

    @Column(nullable = false)
    private LocalDateTime actionAt;

    @PrePersist
    protected void onCreate() {
        this.actionAt = LocalDateTime.now();
    }

    public enum ApprovalAction {
        APPROVE, REJECT
    }
}
