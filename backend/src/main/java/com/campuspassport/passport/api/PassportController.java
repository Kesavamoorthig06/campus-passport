package com.campuspassport.passport.api;

import com.campuspassport.passport.application.PassportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passport")
public class PassportController {
    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "X-User-Email", required = false) String email) {
        var summary = passportService.getPassportSummary(email);
        return ResponseEntity.ok(summary);
    }
}
