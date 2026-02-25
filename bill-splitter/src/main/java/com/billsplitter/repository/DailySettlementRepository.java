package com.billsplitter.repository;
import com.billsplitter.model.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {
    List<DailySettlement> findBySettlementDateOrderByAmountDesc(LocalDate date);
    boolean existsBySettlementDate(LocalDate date);
}
