package com.billsplitter.dto;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class SettlementSummaryDTO {
    private String debtorName;
    private String creditorName;
    private BigDecimal amount;
    private String debtorUsername;
    private String creditorUsername;
}
