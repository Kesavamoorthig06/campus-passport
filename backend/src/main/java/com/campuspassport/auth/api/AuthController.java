package com.campuspassport.auth.api;

import com.campuspassport.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        var result = authService.register(request.email(), request.firstName(), request.lastName(), request.password());
        return ResponseEntity.ok(Map.of(
                "userId", result.userId(),
                "email", result.email(),
                "status", "PENDING_VERIFICATION",
                "verificationCode", result.verificationCode(),
                "message", "Campus email verification is required before your passport becomes visible."
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        var result = authService.verifyEmail(request.email(), request.code());
        return ResponseEntity.ok(Map.of(
                "userId", result.userId(),
                "email", result.email(),
                "status", "VERIFIED",
                "passportReady", true,
                "message", "Campus email verified successfully."
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String email) {
        return ResponseEntity.ok(Map.of(
                "email", email,
                "verified", authService.isVerified(email)
        ));
    }

    public record RegisterRequest(
            String email,
            String firstName,
            String lastName,
            String password
    ) {}

    public record VerifyEmailRequest(
            String email,
            String code
    ) {}
}
