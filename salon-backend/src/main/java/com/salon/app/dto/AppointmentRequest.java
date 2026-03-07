package com.salon.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private Long staffId;

    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDateTime;

    private String notes;
}
