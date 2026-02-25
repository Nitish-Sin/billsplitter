package com.billsplitter.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_settlements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DailySettlement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private LocalDate settlementDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private LocalDateTime concludedAt;
    @Enumerated(EnumType.STRING)
    @Builder.Default private SettlementStatus status = SettlementStatus.PENDING;
    public enum SettlementStatus { PENDING, SETTLED }
}
