package com.salon.app.dto;

import com.salon.app.model.SalonService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currencyCode;
    private String currencySymbol;
    private Integer durationMinutes;
    private String category;
    private String imageUrl;

    public static ServiceResponse fromEntity(SalonService service, BigDecimal localPrice,
                                              String currencyCode, String currencySymbol) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(localPrice)
                .currencyCode(currencyCode)
                .currencySymbol(currencySymbol)
                .durationMinutes(service.getDurationMinutes())
                .category(service.getCategory())
                .imageUrl(service.getImageUrl())
                .build();
    }
}
