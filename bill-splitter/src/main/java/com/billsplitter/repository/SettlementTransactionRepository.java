package com.billsplitter.repository;

import com.billsplitter.model.SettlementTransaction;
import com.billsplitter.model.DaySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, Long> {
    List<SettlementTransaction> findBySettlement(DaySettlement settlement);
}
