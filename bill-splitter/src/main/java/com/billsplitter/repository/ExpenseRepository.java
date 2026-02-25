package com.billsplitter.repository;
import com.billsplitter.model.Expense;
import com.billsplitter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByExpenseDateOrderByCreatedAtDesc(LocalDate date);
    List<Expense> findByStatusAndExpenseDate(Expense.Status status, LocalDate date);
    @Query("SELECT e FROM Expense e WHERE e.expenseDate = :date AND e.status = 'APPROVED' ORDER BY e.createdAt DESC")
    List<Expense> findApprovedExpensesForDate(@Param("date") LocalDate date);
    boolean existsByExpenseDateAndStatus(LocalDate date, Expense.Status status);
}
