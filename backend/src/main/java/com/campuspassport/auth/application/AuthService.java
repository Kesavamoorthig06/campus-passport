package com.campuspassport.auth.application;

import com.campuspassport.auth.domain.VerificationChallenge;
import com.campuspassport.auth.infrastructure.InMemoryVerificationStore;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final InMemoryVerificationStore verificationStore;

    public AuthService(InMemoryVerificationStore verificationStore) {
        this.verificationStore = verificationStore;
    }

    public RegistrationResult register(String email, String firstName, String lastName, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!email.matches("(?i)^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)*(?:campus|student)\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("A verified campus email is required.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        String userId = UUID.randomUUID().toString();
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        VerificationChallenge challenge = new VerificationChallenge(
                userId,
                email.trim(),
                firstName == null ? "Student" : firstName.trim(),
                lastName == null ? "" : lastName.trim(),
                code,
                Instant.now().plusSeconds(900)
        );
        verificationStore.save(challenge);

        return new RegistrationResult(userId, email.trim(), code);
    }

    public VerificationResult verifyEmail(String email, String code) {
        if (email == null || code == null) {
            throw new IllegalArgumentException("Email and verification code are required.");
        }

        VerificationChallenge challenge = verificationStore.get(email.trim());
        if (challenge == null) {
            throw new IllegalArgumentException("No pending verification request exists for this email.");
        }
        if (challenge.expiresAt().isBefore(Instant.now())) {
            verificationStore.delete(email.trim());
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }
        if (!challenge.code().equals(code.trim())) {
            throw new IllegalArgumentException("Verification code does not match.");
        }

        verificationStore.markVerified(email.trim(), challenge);
        return new VerificationResult(challenge.userId(), email.trim());
    }

    public boolean isVerified(String email) {
        return verificationStore.isVerified(email);
    }

    public record RegistrationResult(String userId, String email, String verificationCode) {}

    public record VerificationResult(String userId, String email) {}
}
