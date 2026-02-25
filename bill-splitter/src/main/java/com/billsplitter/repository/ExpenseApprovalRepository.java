package com.billsplitter.repository;

import com.billsplitter.model.ExpenseApproval;
import com.billsplitter.model.Expense;
import com.billsplitter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseApprovalRepository extends JpaRepository<ExpenseApproval, Long> {
    List<ExpenseApproval> findByExpense(Expense expense);
    Optional<ExpenseApproval> findByExpenseAndApprover(Expense expense, User approver);
    boolean existsByExpenseAndApprover(Expense expense, User approver);
    long countByExpenseAndAction(Expense expense, ExpenseApproval.ApprovalAction action);
}
