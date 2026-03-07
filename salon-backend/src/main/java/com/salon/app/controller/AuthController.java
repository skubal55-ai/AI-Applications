package com.salon.app.controller;

import com.salon.app.dto.*;
import com.salon.app.service.AuthService;
import com.salon.app.util.GeoLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GeoLocationUtil geoLocationUtil;

    public AuthController(AuthService authService, GeoLocationUtil geoLocationUtil) {
        this.authService = authService;
        this.geoLocationUtil = geoLocationUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        String clientIp = geoLocationUtil.extractClientIp(
                httpRequest.getHeader("X-Forwarded-For"),
                httpRequest.getRemoteAddr()
        );
        AuthResponse response = authService.register(request, clientIp);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Refresh token is required"));
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }
}
