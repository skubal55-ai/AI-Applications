package com.salon.app.service;

import com.salon.app.dto.CurrencyInfo;
import com.salon.app.dto.SubscriptionRequest;
import com.salon.app.dto.SubscriptionResponse;
import com.salon.app.model.Subscription;
import com.salon.app.model.Subscription.SubscriptionPlan;
import com.salon.app.model.Subscription.SubscriptionStatus;
import com.salon.app.model.User;
import com.salon.app.repository.SubscriptionRepository;
import com.salon.app.repository.UserRepository;
import com.salon.app.util.CurrencyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CurrencyUtil currencyUtil;

    // Base prices in USD for each plan
    private static final Map<SubscriptionPlan, BigDecimal> PLAN_PRICES = new HashMap<>();
    private static final Map<SubscriptionPlan, String> PLAN_FEATURES = new HashMap<>();

    static {
        PLAN_PRICES.put(SubscriptionPlan.BASIC, new BigDecimal("9.99"));
        PLAN_PRICES.put(SubscriptionPlan.PREMIUM, new BigDecimal("19.99"));
        PLAN_PRICES.put(SubscriptionPlan.VIP, new BigDecimal("29.99"));

        PLAN_FEATURES.put(SubscriptionPlan.BASIC, "Basic booking, View services, Email reminders");
        PLAN_FEATURES.put(SubscriptionPlan.PREMIUM, "Priority booking, 10% discount on services, Loyalty points, SMS reminders");
        PLAN_FEATURES.put(SubscriptionPlan.VIP, "All Premium features, Home service option, Free cancellation, 20% discount, Priority support");
    }

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository,
                                CurrencyUtil currencyUtil) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.currencyUtil = currencyUtil;
    }

    @Transactional
    public SubscriptionResponse subscribe(Long customerId, SubscriptionRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if already has active subscription
        Optional<Subscription> existingActive = subscriptionRepository
                .findByCustomerAndStatus(customer, SubscriptionStatus.ACTIVE);
        if (existingActive.isPresent()) {
            throw new RuntimeException("You already have an active subscription. Please cancel it first to switch plans.");
        }

        SubscriptionPlan plan = SubscriptionPlan.valueOf(request.getPlan().toUpperCase());
        BigDecimal basePriceUsd = PLAN_PRICES.get(plan);

        // Convert to user's local currency
        String countryCode = customer.getCountryCode() != null ? customer.getCountryCode() : "US";
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        BigDecimal localPrice = currencyUtil.convertFromUsd(basePriceUsd, countryCode);

        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .customer(customer)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .priceInLocalCurrency(localPrice)
                .currencyCode(currencyInfo.getCurrencyCode())
                .currencySymbol(currencyInfo.getCurrencySymbol())
                .startDate(now)
                .endDate(now.plusMonths(1))
                .autoRenew(request.isAutoRenew())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        return SubscriptionResponse.fromEntity(saved, PLAN_FEATURES.get(plan));
    }

    public SubscriptionResponse getActiveSubscription(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Subscription subscription = subscriptionRepository
                .findByCustomerAndStatus(customer, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        return SubscriptionResponse.fromEntity(subscription, PLAN_FEATURES.get(subscription.getPlan()));
    }

    public List<SubscriptionResponse> getSubscriptionHistory(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return subscriptionRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(s -> SubscriptionResponse.fromEntity(s, PLAN_FEATURES.get(s.getPlan())))
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Subscription subscription = subscriptionRepository
                .findByCustomerAndStatus(customer, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription to cancel"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        Subscription saved = subscriptionRepository.save(subscription);
        return SubscriptionResponse.fromEntity(saved, PLAN_FEATURES.get(saved.getPlan()));
    }

    /**
     * Get subscription plans with prices in user's local currency.
     */
    public List<Map<String, Object>> getAvailablePlans(String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);

        return List.of(
            buildPlanInfo(SubscriptionPlan.BASIC, currencyInfo, countryCode),
            buildPlanInfo(SubscriptionPlan.PREMIUM, currencyInfo, countryCode),
            buildPlanInfo(SubscriptionPlan.VIP, currencyInfo, countryCode)
        );
    }

    private Map<String, Object> buildPlanInfo(SubscriptionPlan plan, CurrencyInfo currencyInfo, String countryCode) {
        BigDecimal localPrice = currencyUtil.convertFromUsd(PLAN_PRICES.get(plan), countryCode);
        Map<String, Object> planInfo = new HashMap<>();
        planInfo.put("plan", plan.name());
        planInfo.put("priceUsd", PLAN_PRICES.get(plan));
        planInfo.put("priceLocal", localPrice);
        planInfo.put("currencyCode", currencyInfo.getCurrencyCode());
        planInfo.put("currencySymbol", currencyInfo.getCurrencySymbol());
        planInfo.put("features", PLAN_FEATURES.get(plan));
        planInfo.put("billingCycle", "Monthly");
        return planInfo;
    }
}
