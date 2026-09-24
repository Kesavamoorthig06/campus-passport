package com.campuspassport.auth.domain;

import java.time.Instant;

public record VerificationChallenge(
        String userId,
        String email,
        String firstName,
        String lastName,
        String code,
        Instant expiresAt
) {}
