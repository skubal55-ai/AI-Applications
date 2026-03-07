package com.salon.app.controller;

import com.salon.app.dto.ApiResponse;
import com.salon.app.dto.ServiceResponse;
import com.salon.app.model.SalonService;
import com.salon.app.service.SalonServiceService;
import com.salon.app.util.GeoLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final SalonServiceService salonServiceService;
    private final GeoLocationUtil geoLocationUtil;

    public ServiceController(SalonServiceService salonServiceService, GeoLocationUtil geoLocationUtil) {
        this.salonServiceService = salonServiceService;
        this.geoLocationUtil = geoLocationUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllServices(
            @RequestParam(required = false) String countryCode,
            HttpServletRequest request) {
        if (countryCode == null || countryCode.isEmpty()) {
            String ip = geoLocationUtil.extractClientIp(
                    request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
            countryCode = geoLocationUtil.getCountryCodeFromIp(ip);
        }
        List<ServiceResponse> services = salonServiceService.getAllActiveServices(countryCode);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved", services));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse> getServicesByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String countryCode,
            HttpServletRequest request) {
        if (countryCode == null || countryCode.isEmpty()) {
            String ip = geoLocationUtil.extractClientIp(
                    request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
            countryCode = geoLocationUtil.getCountryCodeFromIp(ip);
        }
        List<ServiceResponse> services = salonServiceService.getServicesByCategory(category, countryCode);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved", services));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getServiceById(
            @PathVariable Long id,
            @RequestParam(required = false) String countryCode,
            HttpServletRequest request) {
        if (countryCode == null || countryCode.isEmpty()) {
            String ip = geoLocationUtil.extractClientIp(
                    request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
            countryCode = geoLocationUtil.getCountryCodeFromIp(ip);
        }
        ServiceResponse service = salonServiceService.getServiceById(id, countryCode);
        return ResponseEntity.ok(ApiResponse.success("Service retrieved", service));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createService(@RequestBody SalonService service) {
        ServiceResponse created = salonServiceService.createService(service);
        return ResponseEntity.ok(ApiResponse.success("Service created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateService(@PathVariable Long id, @RequestBody SalonService service) {
        ServiceResponse updated = salonServiceService.updateService(id, service);
        return ResponseEntity.ok(ApiResponse.success("Service updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteService(@PathVariable Long id) {
        salonServiceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deactivated"));
    }
}
