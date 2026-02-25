package com.billsplitter.controller;

import com.billsplitter.model.*;
import com.billsplitter.model.ExpenseApproval.ApprovalAction;
import com.billsplitter.repository.ExpenseApprovalRepository;
import com.billsplitter.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;
    private final SettlementService settlementService;
    private final ExpenseApprovalRepository approvalRepository;

    // ─── Dashboard ────────────────────────────────────────────────────────────

   

    // ─── Create Expense ───────────────────────────────────────────────────────

    @PostMapping("/expense/create")
    public String createExpense(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String description,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam List<Long> participantIds,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            expenseService.createExpense(description, amount, currentUser.getId(), participantIds);
            redirectAttributes.addFlashAttribute("success", "Expense posted! Waiting for 3 approvals.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ─── Approve / Reject ─────────────────────────────────────────────────────

    @PostMapping("/expense/{id}/approve")
    public String approveExpense(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam(required = false) String comment,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            expenseService.approveOrReject(id, currentUser.getId(), ApprovalAction.APPROVE, comment);
            redirectAttributes.addFlashAttribute("success", "Expense approved!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/expense/{id}/reject")
    public String rejectExpense(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(required = false) String comment,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            expenseService.approveOrReject(id, currentUser.getId(), ApprovalAction.REJECT, comment);
            redirectAttributes.addFlashAttribute("info", "Expense rejected.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ─── Expense Detail ───────────────────────────────────────────────────────

    @GetMapping("/expense/{id}")
    public String expenseDetail(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Expense expense = expenseService.findById(id).orElseThrow();
        List<ExpenseApproval> approvals = approvalRepository.findByExpense(expense);
        boolean alreadyVoted = approvalRepository.existsByExpenseAndApprover(expense, currentUser);
        boolean isOwner = expense.getPaidBy().getId().equals(currentUser.getId());

        model.addAttribute("expense", expense);
        model.addAttribute("approvals", approvals);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("alreadyVoted", alreadyVoted);
        model.addAttribute("isOwner", isOwner);
        return "expense-detail";
    }

    // ─── Conclude Day ─────────────────────────────────────────────────────────

    @GetMapping("/conclude")
    public String concludePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        LocalDate today = LocalDate.now();

        boolean alreadyConcluded = settlementService.getSettlementForDate(today).isPresent();
        boolean hasApproved = expenseService.hasApprovedUnsettledExpenses(today);
        var preview = expenseService.calculateSettlement(today);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("today", today);
        model.addAttribute("alreadyConcluded", alreadyConcluded);
        model.addAttribute("hasApproved", hasApproved);
        model.addAttribute("settlementPreview", preview);
        return "conclude";
    }

    @PostMapping("/conclude")
    public String concludeDay(@AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        try {
            DaySettlement settlement = settlementService.concludeDay(LocalDate.now(), currentUser.getId());
            return "redirect:/settlement/" + settlement.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/conclude";
        }
    }

    // ─── Settlement View ──────────────────────────────────────────────────────

    @GetMapping("/settlement/{id}")
    public String viewSettlement(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        DaySettlement settlement = settlementService.getAllSettlements().stream()
                .filter(s -> s.getId().equals(id)).findFirst().orElseThrow();
        List<SettlementTransaction> transactions = settlementService.getTransactionsForSettlement(settlement);

        model.addAttribute("settlement", settlement);
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentUser", currentUser);
        return "settlement";
    }

    // ─── History ──────────────────────────────────────────────────────────────

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("settlements", settlementService.getAllSettlements());
        return "history";
    }
}
