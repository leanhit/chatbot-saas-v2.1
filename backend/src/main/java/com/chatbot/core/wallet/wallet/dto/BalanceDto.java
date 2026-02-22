package com.chatbot.core.wallet.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceDto {
    
    private BigDecimal balance;
    private String currency;
    private String status;
    
    public static BalanceDto from(BigDecimal balance, String currency, String status) {
        BalanceDto dto = new BalanceDto();
        dto.setBalance(balance);
        dto.setCurrency(currency);
        dto.setStatus(status);
        return dto;
    }
}
