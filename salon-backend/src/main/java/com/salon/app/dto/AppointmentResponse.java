package com.salon.app.dto;

import com.salon.app.model.Appointment;
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
public class AppointmentResponse {
    private Long id;
    private String customerName;
    private String staffName;
    private String serviceName;
    private BigDecimal servicePrice;
    private String currencyCode;
    private String currencySymbol;
    private Integer durationMinutes;
    private LocalDateTime appointmentDateTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public static AppointmentResponse fromEntity(Appointment appointment, String currencyCode,
                                                  String currencySymbol, BigDecimal localPrice) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .customerName(appointment.getCustomer().getFullName())
                .staffName(appointment.getStaff() != null ? appointment.getStaff().getFullName() : "Any available")
                .serviceName(appointment.getService().getName())
                .servicePrice(localPrice)
                .currencyCode(currencyCode)
                .currencySymbol(currencySymbol)
                .durationMinutes(appointment.getService().getDurationMinutes())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus().name())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
