package com.billsplitter.controller;

import com.billsplitter.model.Expense;
import com.billsplitter.model.User;
import com.billsplitter.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        LocalDate today = LocalDate.now();

        List<Expense> todayExpenses = expenseService.getExpensesForToday();

        List<Expense> pendingExpenses = todayExpenses.stream()
                .filter(e -> e.getStatus() == Expense.Status.PENDING)
                .toList();

        boolean todayConcluded = settlementService.isDayConcluded(today);

        boolean hasApproved = todayExpenses.stream()
                .anyMatch(e -> e.getStatus() == Expense.Status.APPROVED);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("today", today);
        model.addAttribute("todayExpenses", todayExpenses);
        model.addAttribute("pendingExpenses", pendingExpenses);
        model.addAttribute("todayConcluded", todayConcluded); // ⭐ important
        model.addAttribute("hasApproved", hasApproved);       // ⭐ important
        model.addAttribute("allUsers", userService.getAllUsers());

        return "dashboard";
    }
}
