package com.billsplitter.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "day_settlements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DaySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private LocalDateTime concludedAt;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SettlementTransaction> transactions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concluded_by_id")
    private User concludedBy;

    @PrePersist
    protected void onCreate() {
        this.concludedAt = LocalDateTime.now();
    }
}
