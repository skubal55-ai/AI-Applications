package com.salon.app.controller;

import com.salon.app.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    private static final List<Map<String, String>> SUPPORTED_LANGUAGES = new ArrayList<>();

    static {
        SUPPORTED_LANGUAGES.add(Map.of("code", "en", "name", "English", "nativeName", "English"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "hi", "name", "Hindi", "nativeName", "\u0939\u093F\u0928\u094D\u0926\u0940"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "es", "name", "Spanish", "nativeName", "Espa\u00F1ol"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "fr", "name", "French", "nativeName", "Fran\u00E7ais"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "ja", "name", "Japanese", "nativeName", "\u65E5\u672C\u8A9E"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "zh", "name", "Chinese", "nativeName", "\u4E2D\u6587"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "ar", "name", "Arabic", "nativeName", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "pt", "name", "Portuguese", "nativeName", "Portugu\u00EAs"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "de", "name", "German", "nativeName", "Deutsch"));
        SUPPORTED_LANGUAGES.add(Map.of("code", "ko", "name", "Korean", "nativeName", "\uD55C\uAD6D\uC5B4"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getSupportedLanguages() {
        return ResponseEntity.ok(ApiResponse.success("Supported languages", SUPPORTED_LANGUAGES));
    }
}
