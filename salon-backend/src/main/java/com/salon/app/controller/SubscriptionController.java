package com.salon.app.controller;

import com.salon.app.dto.ApiResponse;
import com.salon.app.dto.SubscriptionRequest;
import com.salon.app.dto.SubscriptionResponse;
import com.salon.app.security.CustomUserDetails;
import com.salon.app.service.SubscriptionService;
import com.salon.app.util.GeoLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final GeoLocationUtil geoLocationUtil;

    public SubscriptionController(SubscriptionService subscriptionService, GeoLocationUtil geoLocationUtil) {
        this.subscriptionService = subscriptionService;
        this.geoLocationUtil = geoLocationUtil;
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse> getAvailablePlans(
            @RequestParam(required = false) String countryCode,
            HttpServletRequest request) {
        if (countryCode == null || countryCode.isEmpty()) {
            String ip = geoLocationUtil.extractClientIp(
                    request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
            countryCode = geoLocationUtil.getCountryCodeFromIp(ip);
        }
        List<Map<String, Object>> plans = subscriptionService.getAvailablePlans(countryCode);
        return ResponseEntity.ok(ApiResponse.success("Plans retrieved", plans));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Subscription activated", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SubscriptionResponse response = subscriptionService.getActiveSubscription(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Active subscription retrieved", response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse> getSubscriptionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SubscriptionResponse> history = subscriptionService.getSubscriptionHistory(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Subscription history retrieved", history));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SubscriptionResponse response = subscriptionService.cancelSubscription(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled", response));
    }
}
