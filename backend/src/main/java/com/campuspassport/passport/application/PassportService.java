package com.campuspassport.passport.application;

import com.campuspassport.auth.infrastructure.InMemoryVerificationStore;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PassportService {
    private final InMemoryVerificationStore verificationStore;

    public PassportService(InMemoryVerificationStore verificationStore) {
        this.verificationStore = verificationStore;
    }

    public Map<String, Object> getPassportSummary(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Verified student email is required.");
        }
        if (!verificationStore.isVerified(email)) {
            throw new IllegalArgumentException("Campus email verification is required first.");
        }

        return Map.of(
                "studentId", "student-" + email.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(),
                "email", email,
                "displayName", "Campus Student",
                "status", "VERIFIED",
                "campus", "North Hall Campus",
                "major", "Undeclared",
                "interests", java.util.List.of("Research", "Community", "Campus Events"),
                "connectionCount", 18,
                "communityCount", 5,
                "lastUpdated", java.time.Instant.now()
        );
    }
}
