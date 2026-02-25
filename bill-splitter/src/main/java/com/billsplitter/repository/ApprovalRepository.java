package com.billsplitter.repository;
import com.billsplitter.model.Approval;
import com.billsplitter.model.Expense;
import com.billsplitter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByExpense(Expense expense);
    Optional<Approval> findByExpenseAndApprover(Expense expense, User approver);
    boolean existsByExpenseAndApprover(Expense expense, User approver);
}
