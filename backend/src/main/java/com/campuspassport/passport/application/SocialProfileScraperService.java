package com.campuspassport.passport.application;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SocialProfileScraperService {
    public SocialScrapeResult scrape(String platform, String url) {
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("Platform is required.");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Profile URL is required.");
        }

        String normalizedPlatform = platform.trim().toLowerCase(Locale.ROOT);
        String normalizedUrl = normalizeUrl(normalizedPlatform, url.trim());

        try {
            Document document = Jsoup.connect(normalizedUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36")
                    .timeout(15_000)
                    .followRedirects(true)
                    .get();

            return switch (normalizedPlatform) {
                case "leetcode" -> parseLeetCode(document, normalizedUrl);
                case "github" -> parseGitHub(document, normalizedUrl);
                case "linkedin" -> parseLinkedIn(document, normalizedUrl);
                case "codeforces" -> parseCodeforces(document, normalizedUrl);
                default -> parseGeneric(document, normalizedUrl);
            };
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read profile data for " + normalizedPlatform + ": " + e.getMessage(), e);
        }
    }

    private String normalizeUrl(String platform, String url) {
        String trimmed = url.trim();
        if (trimmed.matches("(?i)^https?://.*")) {
            return trimmed;
        }

        return switch (platform) {
            case "leetcode" -> "https://leetcode.com/" + trimmed.replace("/", "");
            case "github" -> "https://github.com/" + trimmed.replace("/", "");
            case "linkedin" -> "https://www.linkedin.com/in/" + trimmed.replace("/", "");
            case "codeforces" -> "https://codeforces.com/profile/" + trimmed.replace("/", "");
            default -> trimmed;
        };
    }

    private SocialScrapeResult parseGeneric(Document document, String profileUrl) {
        String title = document.title();
        String handle = extractHandle(profileUrl);
        String displayName = extractMeta(document, "og:title");
        if (displayName == null || displayName.isBlank()) {
            displayName = title;
        }
        String bio = extractMeta(document, "description", "og:description", "twitter:description");
        return new SocialScrapeResult(
                "custom",
                handle,
                displayName,
                profileUrl,
                bio == null ? "Public profile" : bio,
                Map.of("source", "public_html"),
                "SCRAPED"
        );
    }

    private SocialScrapeResult parseLeetCode(Document document, String profileUrl) {
        String handle = extractHandle(profileUrl);
        if (handle == null || handle.isBlank()) {
            handle = extractPattern(document.html(), "\\\"username\\\":\\\"([^\\\"]+)\\\"");
        }

        String displayName = extractMeta(document, "og:title", "twitter:title");
        if (displayName == null || displayName.isBlank()) {
            Element h1 = document.selectFirst("h1");
            displayName = h1 == null ? handle : h1.text();
        }
        if (displayName != null && displayName.contains("- LeetCode")) {
            displayName = displayName.replace(" - LeetCode", "");
        }

        String bio = extractMeta(document, "description", "og:description");
        String totalSolved = extractPattern(document.html(), "\\\"totalSolved\\\":(\\d+)");
        String easy = extractPattern(document.html(), "\\\"easySolved\\\":(\\d+)");
        String medium = extractPattern(document.html(), "\\\"mediumSolved\\\":(\\d+)");
        String hard = extractPattern(document.html(), "\\\"hardSolved\\\":(\\d+)");

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSolved", totalSolved == null ? "unknown" : totalSolved);
        stats.put("easySolved", easy == null ? "unknown" : easy);
        stats.put("mediumSolved", medium == null ? "unknown" : medium);
        stats.put("hardSolved", hard == null ? "unknown" : hard);

        return new SocialScrapeResult(
                "leetcode",
                handle == null ? "unknown" : handle,
                displayName == null ? handle : displayName,
                profileUrl,
                bio == null ? "LeetCode profile" : bio,
                stats,
                "SCRAPED"
        );
    }

    private SocialScrapeResult parseGitHub(Document document, String profileUrl) {
        String handle = extractHandle(profileUrl);
        String displayName = extractMeta(document, "og:title", "twitter:title");
        if (displayName != null && displayName.contains("· GitHub")) {
            displayName = displayName.replace(" · GitHub", "");
        }
        String bio = extractMeta(document, "description", "og:description");

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("profile", "public_github_profile");
        stats.put("followers", extractPattern(document.html(), "followers\\s*[:=]\\s*([0-9,]+)"));
        stats.put("following", extractPattern(document.html(), "following\\s*[:=]\\s*([0-9,]+)"));
        stats.put("publicRepos", extractPattern(document.html(), "public_repos\\s*[:=]\\s*([0-9,]+)"));

        return new SocialScrapeResult(
                "github",
                handle == null ? "unknown" : handle,
                displayName == null ? handle : displayName,
                profileUrl,
                bio == null ? "GitHub profile" : bio,
                stats,
                "SCRAPED"
        );
    }

    private SocialScrapeResult parseLinkedIn(Document document, String profileUrl) {
        String handle = extractHandle(profileUrl);
        String displayName = extractMeta(document, "og:title", "twitter:title");
        String bio = extractMeta(document, "description", "og:description");
        return new SocialScrapeResult(
                "linkedin",
                handle == null ? "unknown" : handle,
                displayName == null ? handle : displayName,
                profileUrl,
                bio == null ? "LinkedIn profile" : bio,
                Map.of("source", "public_html"),
                "SCRAPED"
        );
    }

    private SocialScrapeResult parseCodeforces(Document document, String profileUrl) {
        String handle = extractHandle(profileUrl);
        String displayName = document.title();
        if (displayName != null && displayName.contains("- Codeforces")) {
            displayName = displayName.replace(" - Codeforces", "");
        }
        String bio = extractMeta(document, "description", "og:description");
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("rating", extractPattern(document.html(), "\"rating\":(\\d+)"));
        stats.put("maxRating", extractPattern(document.html(), "\"maxRating\":(\\d+)"));
        return new SocialScrapeResult(
                "codeforces",
                handle == null ? "unknown" : handle,
                displayName == null ? handle : displayName,
                profileUrl,
                bio == null ? "Codeforces profile" : bio,
                stats,
                "SCRAPED"
        );
    }

    private String extractHandle(String url) {
        String cleaned = url.replace("https://", "").replace("http://", "");
        int idx = cleaned.indexOf("//");
        if (idx >= 0) {
            cleaned = cleaned.substring(idx + 2);
        }
        cleaned = cleaned.replace("www.", "");

        String[] segments = cleaned.split("/");
        if (segments.length < 2) {
            return cleaned;
        }

        if (segments[0].contains("linkedin.com")) {
            return segments[2];
        }
        if (segments[0].contains("github.com") || segments[0].contains("leetcode.com") || segments[0].contains("codeforces.com")) {
            return segments[1];
        }
        return segments[0];
    }

    private String extractMeta(Document document, String... names) {
        for (String name : names) {
            Element meta = document.selectFirst("meta[property=" + name + "]");
            if (meta == null) {
                meta = document.selectFirst("meta[name=" + name + "]");
            }
            if (meta != null && meta.hasAttr("content") && !meta.attr("content").isBlank()) {
                return meta.attr("content");
            }
        }
        return null;
    }

    private String extractPattern(String input, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) == null ? matcher.group() : matcher.group(1);
    }

    public record SocialScrapeResult(
            String platform,
            String handle,
            String displayName,
            String profileUrl,
            String bio,
            Map<String, Object> stats,
            String status
    ) {}
}
