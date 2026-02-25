package com.billsplitter.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approvals",
    uniqueConstraints = @UniqueConstraint(columnNames = {"expense_id", "approver_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Approval {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Decision decision;
    @Column private String comment;
    @Column(nullable = false) private LocalDateTime decidedAt;
    public enum Decision { APPROVED, REJECTED }
}
