package com.salon.app.service;

import com.salon.app.dto.*;
import com.salon.app.model.User;
import com.salon.app.repository.UserRepository;
import com.salon.app.security.CustomUserDetails;
import com.salon.app.security.JwtTokenProvider;
import com.salon.app.util.CurrencyUtil;
import com.salon.app.util.GeoLocationUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CurrencyUtil currencyUtil;
    private final GeoLocationUtil geoLocationUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
                       CurrencyUtil currencyUtil, GeoLocationUtil geoLocationUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.currencyUtil = currencyUtil;
        this.geoLocationUtil = geoLocationUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientIp) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Determine country code from IP if not provided
        String countryCode = request.getCountryCode();
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = geoLocationUtil.getCountryCodeFromIp(clientIp);
        }

        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.CUSTOMER)
                .countryCode(countryCode)
                .currencyCode(currencyInfo.getCurrencyCode())
                .build();

        userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .currencyCode(currencyInfo.getCurrencyCode())
                .currencySymbol(currencyInfo.getCurrencySymbol())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails.getUsername());

        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(userDetails.getCountryCode());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userDetails.getId())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .role(userDetails.getRole().name())
                .currencyCode(currencyInfo.getCurrencyCode())
                .currencySymbol(currencyInfo.getCurrencySymbol())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = tokenProvider.generateAccessToken(email);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(user.getCountryCode());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .currencyCode(currencyInfo.getCurrencyCode())
                .currencySymbol(currencyInfo.getCurrencySymbol())
                .build();
    }
}
