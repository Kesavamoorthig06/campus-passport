package com.campuspassport.auth.infrastructure;

import com.campuspassport.auth.domain.VerificationChallenge;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryVerificationStore {
    private final Map<String, VerificationChallenge> pendingChallenges = new ConcurrentHashMap<>();
    private final Map<String, String> verifiedUsers = new ConcurrentHashMap<>();

    public void save(VerificationChallenge challenge) {
        pendingChallenges.put(challenge.email().toLowerCase(), challenge);
    }

    public VerificationChallenge get(String email) {
        return pendingChallenges.get(email.toLowerCase());
    }

    public void delete(String email) {
        pendingChallenges.remove(email.toLowerCase());
    }

    public void markVerified(String email, VerificationChallenge challenge) {
        pendingChallenges.remove(email.toLowerCase());
        verifiedUsers.put(email.toLowerCase(), challenge.userId());
    }

    public boolean isVerified(String email) {
        return verifiedUsers.containsKey(email.toLowerCase());
    }
}
