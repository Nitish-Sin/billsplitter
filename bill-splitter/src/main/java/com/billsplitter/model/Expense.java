package com.billsplitter.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String description;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spent_by_id", nullable = false)
    private User spentBy;
    @ManyToMany
    @JoinTable(name = "expense_beneficiaries",
        joinColumns = @JoinColumn(name = "expense_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default private List<User> spentFor = new ArrayList<>();
    @Column(nullable = false) private LocalDate expenseDate;
    @Column(nullable = false) private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default private Status status = Status.PENDING;
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    @Builder.Default private List<Approval> approvals = new ArrayList<>();
    @Column private LocalDateTime concludedAt;
    public enum Status { PENDING, APPROVED, REJECTED, CONCLUDED }
    public int getApprovalCount() {
        return (int) approvals.stream().filter(a -> a.getDecision() == Approval.Decision.APPROVED).count();
    }
    public boolean hasUserApproved(Long userId) {
        return approvals.stream().anyMatch(a -> a.getApprover().getId().equals(userId));
    }
}
