package com.salon.app.dto;

import com.salon.app.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    private Long id;
    private String plan;
    private String status;
    private BigDecimal price;
    private String currencyCode;
    private String currencySymbol;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean autoRenew;
    private String features;

    public static SubscriptionResponse fromEntity(Subscription subscription, String features) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .plan(subscription.getPlan().name())
                .status(subscription.getStatus().name())
                .price(subscription.getPriceInLocalCurrency())
                .currencyCode(subscription.getCurrencyCode())
                .currencySymbol(subscription.getCurrencySymbol())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.isAutoRenew())
                .features(features)
                .build();
    }
}
