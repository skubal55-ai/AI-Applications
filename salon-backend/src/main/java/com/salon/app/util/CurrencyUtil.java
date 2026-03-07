package com.salon.app.util;

import com.salon.app.dto.CurrencyInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for currency detection and conversion based on user location.
 * Maps country codes to their respective currencies and provides approximate exchange rates from USD.
 */
@Component
public class CurrencyUtil {

    private static final Map<String, CurrencyInfo> COUNTRY_CURRENCY_MAP = new HashMap<>();

    static {
        // Asia
        COUNTRY_CURRENCY_MAP.put("IN", new CurrencyInfo("IN", "India", "INR", "₹", new BigDecimal("83.50")));
        COUNTRY_CURRENCY_MAP.put("JP", new CurrencyInfo("JP", "Japan", "JPY", "¥", new BigDecimal("149.50")));
        COUNTRY_CURRENCY_MAP.put("CN", new CurrencyInfo("CN", "China", "CNY", "¥", new BigDecimal("7.24")));
        COUNTRY_CURRENCY_MAP.put("KR", new CurrencyInfo("KR", "South Korea", "KRW", "₩", new BigDecimal("1330.00")));
        COUNTRY_CURRENCY_MAP.put("SG", new CurrencyInfo("SG", "Singapore", "SGD", "S$", new BigDecimal("1.34")));
        COUNTRY_CURRENCY_MAP.put("MY", new CurrencyInfo("MY", "Malaysia", "MYR", "RM", new BigDecimal("4.72")));
        COUNTRY_CURRENCY_MAP.put("TH", new CurrencyInfo("TH", "Thailand", "THB", "฿", new BigDecimal("35.50")));
        COUNTRY_CURRENCY_MAP.put("ID", new CurrencyInfo("ID", "Indonesia", "IDR", "Rp", new BigDecimal("15700.00")));
        COUNTRY_CURRENCY_MAP.put("PH", new CurrencyInfo("PH", "Philippines", "PHP", "₱", new BigDecimal("56.50")));
        COUNTRY_CURRENCY_MAP.put("VN", new CurrencyInfo("VN", "Vietnam", "VND", "₫", new BigDecimal("24500.00")));
        COUNTRY_CURRENCY_MAP.put("PK", new CurrencyInfo("PK", "Pakistan", "PKR", "₨", new BigDecimal("278.00")));
        COUNTRY_CURRENCY_MAP.put("BD", new CurrencyInfo("BD", "Bangladesh", "BDT", "৳", new BigDecimal("110.00")));
        COUNTRY_CURRENCY_MAP.put("LK", new CurrencyInfo("LK", "Sri Lanka", "LKR", "Rs", new BigDecimal("320.00")));
        COUNTRY_CURRENCY_MAP.put("AE", new CurrencyInfo("AE", "UAE", "AED", "د.إ", new BigDecimal("3.67")));
        COUNTRY_CURRENCY_MAP.put("SA", new CurrencyInfo("SA", "Saudi Arabia", "SAR", "﷼", new BigDecimal("3.75")));

        // Europe
        COUNTRY_CURRENCY_MAP.put("GB", new CurrencyInfo("GB", "United Kingdom", "GBP", "£", new BigDecimal("0.79")));
        COUNTRY_CURRENCY_MAP.put("DE", new CurrencyInfo("DE", "Germany", "EUR", "€", new BigDecimal("0.92")));
        COUNTRY_CURRENCY_MAP.put("FR", new CurrencyInfo("FR", "France", "EUR", "€", new BigDecimal("0.92")));
        COUNTRY_CURRENCY_MAP.put("IT", new CurrencyInfo("IT", "Italy", "EUR", "€", new BigDecimal("0.92")));
        COUNTRY_CURRENCY_MAP.put("ES", new CurrencyInfo("ES", "Spain", "EUR", "€", new BigDecimal("0.92")));
        COUNTRY_CURRENCY_MAP.put("NL", new CurrencyInfo("NL", "Netherlands", "EUR", "€", new BigDecimal("0.92")));
        COUNTRY_CURRENCY_MAP.put("CH", new CurrencyInfo("CH", "Switzerland", "CHF", "CHF", new BigDecimal("0.88")));
        COUNTRY_CURRENCY_MAP.put("SE", new CurrencyInfo("SE", "Sweden", "SEK", "kr", new BigDecimal("10.50")));
        COUNTRY_CURRENCY_MAP.put("NO", new CurrencyInfo("NO", "Norway", "NOK", "kr", new BigDecimal("10.80")));
        COUNTRY_CURRENCY_MAP.put("DK", new CurrencyInfo("DK", "Denmark", "DKK", "kr", new BigDecimal("6.88")));
        COUNTRY_CURRENCY_MAP.put("PL", new CurrencyInfo("PL", "Poland", "PLN", "zł", new BigDecimal("4.05")));
        COUNTRY_CURRENCY_MAP.put("RU", new CurrencyInfo("RU", "Russia", "RUB", "₽", new BigDecimal("92.00")));
        COUNTRY_CURRENCY_MAP.put("TR", new CurrencyInfo("TR", "Turkey", "TRY", "₺", new BigDecimal("30.50")));

        // Americas
        COUNTRY_CURRENCY_MAP.put("US", new CurrencyInfo("US", "United States", "USD", "$", BigDecimal.ONE));
        COUNTRY_CURRENCY_MAP.put("CA", new CurrencyInfo("CA", "Canada", "CAD", "C$", new BigDecimal("1.36")));
        COUNTRY_CURRENCY_MAP.put("MX", new CurrencyInfo("MX", "Mexico", "MXN", "$", new BigDecimal("17.20")));
        COUNTRY_CURRENCY_MAP.put("BR", new CurrencyInfo("BR", "Brazil", "BRL", "R$", new BigDecimal("4.97")));
        COUNTRY_CURRENCY_MAP.put("AR", new CurrencyInfo("AR", "Argentina", "ARS", "$", new BigDecimal("870.00")));
        COUNTRY_CURRENCY_MAP.put("CO", new CurrencyInfo("CO", "Colombia", "COP", "$", new BigDecimal("3950.00")));
        COUNTRY_CURRENCY_MAP.put("CL", new CurrencyInfo("CL", "Chile", "CLP", "$", new BigDecimal("950.00")));

        // Oceania
        COUNTRY_CURRENCY_MAP.put("AU", new CurrencyInfo("AU", "Australia", "AUD", "A$", new BigDecimal("1.53")));
        COUNTRY_CURRENCY_MAP.put("NZ", new CurrencyInfo("NZ", "New Zealand", "NZD", "NZ$", new BigDecimal("1.64")));

        // Africa
        COUNTRY_CURRENCY_MAP.put("ZA", new CurrencyInfo("ZA", "South Africa", "ZAR", "R", new BigDecimal("18.80")));
        COUNTRY_CURRENCY_MAP.put("NG", new CurrencyInfo("NG", "Nigeria", "NGN", "₦", new BigDecimal("1550.00")));
        COUNTRY_CURRENCY_MAP.put("KE", new CurrencyInfo("KE", "Kenya", "KES", "KSh", new BigDecimal("153.00")));
        COUNTRY_CURRENCY_MAP.put("EG", new CurrencyInfo("EG", "Egypt", "EGP", "E£", new BigDecimal("30.90")));
    }

    /**
     * Get currency info for a given country code (ISO 3166-1 alpha-2).
     * Falls back to USD if country code is not found.
     */
    public CurrencyInfo getCurrencyByCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return getDefaultCurrency();
        }
        return COUNTRY_CURRENCY_MAP.getOrDefault(countryCode.toUpperCase(), getDefaultCurrency());
    }

    /**
     * Convert a USD amount to the local currency based on country code.
     */
    public BigDecimal convertFromUsd(BigDecimal amountUsd, String countryCode) {
        CurrencyInfo currencyInfo = getCurrencyByCountryCode(countryCode);
        return amountUsd.multiply(currencyInfo.getExchangeRateFromUsd())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get default currency (USD).
     */
    public CurrencyInfo getDefaultCurrency() {
        return COUNTRY_CURRENCY_MAP.get("US");
    }

    /**
     * Get all supported currencies.
     */
    public Map<String, CurrencyInfo> getAllCurrencies() {
        return new HashMap<>(COUNTRY_CURRENCY_MAP);
    }
}
