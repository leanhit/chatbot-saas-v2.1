package com.chatbot.core.billing.currency.model;

/**
 * Supported currencies for billing
 */
public enum Currency {
    USD("United States Dollar", "$", "USD", 840),
    VND("Vietnamese Dong", "₫", "VND", 704),
    EUR("Euro", "€", "EUR", 978),
    GBP("British Pound", "£", "GBP", 826),
    JPY("Japanese Yen", "¥", "JPY", 392);

    private final String displayName;
    private final String symbol;
    private final String code;
    private final int numericCode;

    Currency(String displayName, String symbol, String code, int numericCode) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.code = code;
        this.numericCode = numericCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCode() {
        return code;
    }

    public int getNumericCode() {
        return numericCode;
    }

    public static Currency fromCode(String code) {
        for (Currency currency : values()) {
            if (currency.code.equals(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }
}
