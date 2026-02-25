package com.billsplitter.service;

import com.billsplitter.dto.SettlementSummaryDTO;
import com.billsplitter.model.DailySettlement;
import com.billsplitter.model.Expense;
import com.billsplitter.model.User;
import com.billsplitter.repository.DailySettlementRepository;
import com.billsplitter.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final ExpenseRepository expenseRepository;
    private final DailySettlementRepository settlementRepository;

    /**
     * Calculate and save settlements for today.
     * Only APPROVED expenses are included.
     */
    @Transactional
    public List<DailySettlement> concludeDay(LocalDate date, List<User> allUsers) {
        if (settlementRepository.existsBySettlementDate(date)) {
            throw new IllegalStateException("Day " + date + " has already been concluded");
        }

        List<Expense> approvedExpenses = expenseRepository.findApprovedExpensesForDate(date);

        // net[userId] = how much they paid - how much they owe
        // positive = others owe them; negative = they owe others
        Map<Long, BigDecimal> netBalance = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for (User u : allUsers) {
            netBalance.put(u.getId(), BigDecimal.ZERO);
            userMap.put(u.getId(), u);
        }

        for (Expense expense : approvedExpenses) {
            BigDecimal total = expense.getAmount();
            List<User> beneficiaries = expense.getSpentFor();
            if (beneficiaries.isEmpty()) continue;

            BigDecimal share = total.divide(
                BigDecimal.valueOf(beneficiaries.size()), 2, RoundingMode.HALF_UP);

            // Payer gets credited full amount
            Long payerId = expense.getSpentBy().getId();
            netBalance.merge(payerId, total, BigDecimal::add);
            userMap.put(payerId, expense.getSpentBy());

            // Each beneficiary is debited their share
            for (User beneficiary : beneficiaries) {
                Long bid = beneficiary.getId();
                netBalance.merge(bid, share.negate(), BigDecimal::add);
                userMap.put(bid, beneficiary);
            }
        }

        // Simplify debts using greedy algorithm
        List<DailySettlement> settlements = simplifyDebts(netBalance, userMap, date);

        // Mark approved expenses as CONCLUDED
        for (Expense expense : approvedExpenses) {
            expense.setStatus(Expense.Status.CONCLUDED);
            expense.setConcludedAt(LocalDateTime.now());
        }
        expenseRepository.saveAll(approvedExpenses);

        return settlementRepository.saveAll(settlements);
    }

    private List<DailySettlement> simplifyDebts(
            Map<Long, BigDecimal> netBalance,
            Map<Long, User> userMap,
            LocalDate date) {

        List<DailySettlement> result = new ArrayList<>();

        // Split into creditors (positive) and debtors (negative)
        List<Map.Entry<Long, BigDecimal>> creditors = netBalance.entrySet().stream()
            .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
            .collect(Collectors.toList());

        List<Map.Entry<Long, BigDecimal>> debtors = netBalance.entrySet().stream()
            .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) < 0)
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toList());

        int ci = 0, di = 0;
        BigDecimal[] creditAmounts = creditors.stream()
            .map(Map.Entry::getValue).toArray(BigDecimal[]::new);
        BigDecimal[] debtAmounts = debtors.stream()
            .map(e -> e.getValue().negate()).toArray(BigDecimal[]::new);

        while (ci < creditors.size() && di < debtors.size()) {
            BigDecimal credit = creditAmounts[ci];
            BigDecimal debt = debtAmounts[di];
            BigDecimal settled = credit.min(debt);

            if (settled.compareTo(new BigDecimal("0.01")) > 0) {
                User creditor = userMap.get(creditors.get(ci).getKey());
                User debtor = userMap.get(debtors.get(di).getKey());

                result.add(DailySettlement.builder()
                    .settlementDate(date)
                    .debtor(debtor)
                    .creditor(creditor)
                    .amount(settled.setScale(2, RoundingMode.HALF_UP))
                    .concludedAt(LocalDateTime.now())
                    .build());
            }

            creditAmounts[ci] = credit.subtract(settled);
            debtAmounts[di] = debt.subtract(settled);

            if (creditAmounts[ci].compareTo(new BigDecimal("0.01")) < 0) ci++;
            if (debtAmounts[di].compareTo(new BigDecimal("0.01")) < 0) di++;
        }

        return result;
    }

    public List<DailySettlement> getSettlementsForDate(LocalDate date) {
        return settlementRepository.findBySettlementDateOrderByAmountDesc(date);
    }

    public boolean isDayConcluded(LocalDate date) {
        return settlementRepository.existsBySettlementDate(date);
    }

    public List<SettlementSummaryDTO> getSettlementSummary(LocalDate date) {
        return getSettlementsForDate(date).stream()
            .map(s -> new SettlementSummaryDTO(
                s.getDebtor().getDisplayName(),
                s.getCreditor().getDisplayName(),
                s.getAmount(),
                s.getDebtor().getUsername(),
                s.getCreditor().getUsername()))
            .collect(Collectors.toList());
    }
}
