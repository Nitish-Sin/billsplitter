package com.billsplitter.repository;

import com.billsplitter.model.DaySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface DaySettlementRepository extends JpaRepository<DaySettlement, Long> {
    Optional<DaySettlement> findBySettlementDate(LocalDate date);
    boolean existsBySettlementDate(LocalDate date);
    List<DaySettlement> findAllByOrderBySettlementDateDesc();
}
