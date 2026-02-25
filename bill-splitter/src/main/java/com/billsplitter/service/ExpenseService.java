package com.billsplitter.service;

import com.billsplitter.dto.ExpenseDTO;
import com.billsplitter.model.Approval;
import com.billsplitter.model.Expense;
import com.billsplitter.model.User;
import com.billsplitter.repository.ApprovalRepository;
import com.billsplitter.repository.ExpenseRepository;
import com.billsplitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    @Value("${app.min-approvals-required:3}")
    private int minApprovalsRequired;

    @Transactional
    public Expense createExpense(ExpenseDTO dto, User spentBy) {
        List<User> spentFor = userRepository.findAllById(dto.getSpentForIds());
        if (spentFor.isEmpty()) {
            throw new IllegalArgumentException("At least one person must be selected");
        }

        Expense expense = Expense.builder()
            .description(dto.getDescription())
            .amount(dto.getAmount())
            .spentBy(spentBy)
            .spentFor(spentFor)
            .expenseDate(LocalDate.now())
            .createdAt(LocalDateTime.now())
            .status(Expense.Status.PENDING)
            .build();

        return expenseRepository.save(expense);
    }

    @Transactional
    public Approval approveExpense(Long expenseId, User approver, String comment) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (expense.getStatus() != Expense.Status.PENDING) {
            throw new IllegalStateException("Expense is no longer pending");
        }
        if (expense.getSpentBy().getId().equals(approver.getId())) {
            throw new IllegalStateException("You cannot approve your own expense");
        }
        if (approvalRepository.existsByExpenseAndApprover(expense, approver)) {
            throw new IllegalStateException("You have already voted on this expense");
        }

        Approval approval = Approval.builder()
            .expense(expense)
            .approver(approver)
            .decision(Approval.Decision.APPROVED)
            .comment(comment)
            .decidedAt(LocalDateTime.now())
            .build();

        approvalRepository.save(approval);

        // Refresh approvals count
        List<Approval> allApprovals = approvalRepository.findByExpense(expense);
        long approvedCount = allApprovals.stream()
            .filter(a -> a.getDecision() == Approval.Decision.APPROVED).count();

        if (approvedCount >= minApprovalsRequired) {
            expense.setStatus(Expense.Status.APPROVED);
            expenseRepository.save(expense);
        }

        return approval;
    }

    @Transactional
    public void rejectExpense(Long expenseId, User approver, String comment) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (expense.getStatus() != Expense.Status.PENDING) {
            throw new IllegalStateException("Expense is no longer pending");
        }
        if (expense.getSpentBy().getId().equals(approver.getId())) {
            throw new IllegalStateException("You cannot reject your own expense");
        }
        if (approvalRepository.existsByExpenseAndApprover(expense, approver)) {
            throw new IllegalStateException("You have already voted on this expense");
        }

        Approval approval = Approval.builder()
            .expense(expense)
            .approver(approver)
            .decision(Approval.Decision.REJECTED)
            .comment(comment)
            .decidedAt(LocalDateTime.now())
            .build();

        approvalRepository.save(approval);
        expense.setStatus(Expense.Status.REJECTED);
        expenseRepository.save(expense);
    }

    public List<Expense> getExpensesForToday() {
        return expenseRepository.findByExpenseDateOrderByCreatedAtDesc(LocalDate.now());
    }

    public List<Expense> getApprovedExpensesForDate(LocalDate date) {
        return expenseRepository.findApprovedExpensesForDate(date);
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public List<Approval> getApprovalsForExpense(Long expenseId) {
        Expense expense = findById(expenseId);
        return approvalRepository.findByExpense(expense);
    }

    public int getMinApprovalsRequired() {
        return minApprovalsRequired;
    }
}
