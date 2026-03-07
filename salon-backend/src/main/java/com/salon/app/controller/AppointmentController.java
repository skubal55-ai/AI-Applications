package com.salon.app.controller;

import com.salon.app.dto.ApiResponse;
import com.salon.app.dto.AppointmentRequest;
import com.salon.app.dto.AppointmentResponse;
import com.salon.app.security.CustomUserDetails;
import com.salon.app.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AppointmentResponse> appointments = appointmentService.getCustomerAppointments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse> getUpcomingAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AppointmentResponse> appointments = appointmentService.getUpcomingAppointments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Upcoming appointments retrieved", appointments));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse> getStaffAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AppointmentResponse> appointments = appointmentService.getStaffAppointments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Staff appointments retrieved", appointments));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        AppointmentResponse response = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AppointmentResponse response = appointmentService.cancelAppointment(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", response));
    }
}
