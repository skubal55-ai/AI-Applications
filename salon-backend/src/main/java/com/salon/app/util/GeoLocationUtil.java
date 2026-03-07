package com.salon.app.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Utility for determining user's country code from their IP address.
 * Uses free IP geolocation API (ip-api.com) for lookup.
 */
@Component
public class GeoLocationUtil {

    private static final Logger log = LoggerFactory.getLogger(GeoLocationUtil.class);
    private static final String IP_API_URL = "http://ip-api.com/json/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeoLocationUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Get country code from IP address using free geolocation API.
     * Returns "US" as default if lookup fails.
     */
    public String getCountryCodeFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || isLocalIp(ipAddress)) {
            log.debug("Local or empty IP detected: {}, defaulting to US", ipAddress);
            return "US";
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IP_API_URL + ipAddress + "?fields=countryCode,status"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                String status = json.path("status").asText();
                if ("success".equals(status)) {
                    String countryCode = json.path("countryCode").asText("US");
                    log.debug("IP {} resolved to country: {}", ipAddress, countryCode);
                    return countryCode;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to resolve IP geolocation for {}: {}", ipAddress, e.getMessage());
        }

        return "US";
    }

    /**
     * Extract client IP from request headers (handles proxies).
     */
    public String extractClientIp(String xForwardedFor, String remoteAddr) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; the first one is the client
            return xForwardedFor.split(",")[0].trim();
        }
        return remoteAddr;
    }

    private boolean isLocalIp(String ip) {
        return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("192.168.") || ip.startsWith("10.")
                || ip.startsWith("172.16.") || ip.equals("localhost");
    }
}
