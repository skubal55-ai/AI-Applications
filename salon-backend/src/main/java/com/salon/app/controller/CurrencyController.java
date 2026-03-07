package com.salon.app.controller;

import com.salon.app.dto.ApiResponse;
import com.salon.app.dto.CurrencyInfo;
import com.salon.app.util.CurrencyUtil;
import com.salon.app.util.GeoLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyUtil currencyUtil;
    private final GeoLocationUtil geoLocationUtil;

    public CurrencyController(CurrencyUtil currencyUtil, GeoLocationUtil geoLocationUtil) {
        this.currencyUtil = currencyUtil;
        this.geoLocationUtil = geoLocationUtil;
    }

    /**
     * Auto-detect user's currency based on their IP address.
     */
    @GetMapping("/detect")
    public ResponseEntity<ApiResponse> detectCurrency(HttpServletRequest request) {
        String clientIp = geoLocationUtil.extractClientIp(
                request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
        String countryCode = geoLocationUtil.getCountryCodeFromIp(clientIp);
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        return ResponseEntity.ok(ApiResponse.success("Currency detected", currencyInfo));
    }

    /**
     * Get currency info for a specific country code.
     */
    @GetMapping("/{countryCode}")
    public ResponseEntity<ApiResponse> getCurrencyByCountry(@PathVariable String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        return ResponseEntity.ok(ApiResponse.success("Currency info retrieved", currencyInfo));
    }

    /**
     * Convert an amount from USD to a specified currency.
     */
    @GetMapping("/convert")
    public ResponseEntity<ApiResponse> convertCurrency(
            @RequestParam BigDecimal amount,
            @RequestParam String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        BigDecimal converted = currencyUtil.convertFromUsd(amount, countryCode);

        Map<String, Object> result = Map.of(
                "originalAmount", amount,
                "originalCurrency", "USD",
                "convertedAmount", converted,
                "targetCurrency", currencyInfo.getCurrencyCode(),
                "currencySymbol", currencyInfo.getCurrencySymbol(),
                "exchangeRate", currencyInfo.getExchangeRateFromUsd()
        );

        return ResponseEntity.ok(ApiResponse.success("Conversion complete", result));
    }

    /**
     * Get all supported currencies.
     */
    @GetMapping("/supported")
    public ResponseEntity<ApiResponse> getSupportedCurrencies() {
        return ResponseEntity.ok(ApiResponse.success("Supported currencies", currencyUtil.getAllCurrencies()));
    }
}
