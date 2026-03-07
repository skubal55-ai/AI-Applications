package com.salon.app.controller;

import com.salon.app.dto.ApiResponse;
import com.salon.app.model.Staff;
import com.salon.app.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllAvailableStaff() {
        List<Staff> staffList = staffService.getAllAvailableStaff();
        List<Map<String, Object>> response = staffList.stream()
                .map(this::toStaffMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getStaffById(@PathVariable Long id) {
        Staff staff = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", toStaffMap(staff)));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<ApiResponse> getStaffBySpecialization(@PathVariable String specialization) {
        List<Staff> staffList = staffService.getStaffBySpecialization(specialization);
        List<Map<String, Object>> response = staffList.stream()
                .map(this::toStaffMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved", response));
    }

    private Map<String, Object> toStaffMap(Staff staff) {
        return Map.of(
                "id", staff.getId(),
                "name", staff.getUser().getFullName(),
                "specialization", staff.getSpecialization() != null ? staff.getSpecialization() : "",
                "bio", staff.getBio() != null ? staff.getBio() : "",
                "rating", staff.getRating() != null ? staff.getRating() : 0.0,
                "totalReviews", staff.getTotalReviews() != null ? staff.getTotalReviews() : 0,
                "available", staff.isAvailable()
        );
    }
}
