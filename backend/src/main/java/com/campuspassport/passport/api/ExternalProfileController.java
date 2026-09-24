package com.campuspassport.passport.api;

import com.campuspassport.passport.application.SocialProfileScraperService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/passport")
public class ExternalProfileController {
    private final SocialProfileScraperService scraperService;

    public ExternalProfileController(SocialProfileScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/scrape-social")
    public ResponseEntity<?> scrapeSocialProfile(@Valid @RequestBody ScrapeSocialRequest request) {
        var result = scraperService.scrape(request.platform(), request.url());
        return ResponseEntity.ok(Map.of(
                "platform", result.platform(),
                "handle", result.handle(),
                "displayName", result.displayName(),
                "profileUrl", result.profileUrl(),
                "bio", result.bio(),
                "stats", result.stats(),
                "status", result.status()
        ));
    }

    public record ScrapeSocialRequest(
            String platform,
            String url
    ) {}
}
