package com.salon.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyInfo {
    private String countryCode;
    private String countryName;
    private String currencyCode;
    private String currencySymbol;
    private BigDecimal exchangeRateFromUsd;
}
