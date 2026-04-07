package com.prithvi.url_shortener.service;

import com.prithvi.url_shortener.model.Url;
import com.prithvi.url_shortener.repository.UrlRepository;
import dto.UrlAnalyticsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;

@Service
public class UrlService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final char[] SHORT_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
    private static final int SHORT_CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository repository;
    private final RedisCacheService cacheService;

    public UrlService(UrlRepository repository, RedisCacheService cacheService) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    private String generateShortCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = randomShortCode();
            if (!repository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique short code");
    }

    private String randomShortCode() {
        StringBuilder builder = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            builder.append(SHORT_CODE_ALPHABET[RANDOM.nextInt(SHORT_CODE_ALPHABET.length)]);
        }
        return builder.toString();
    }

    @Transactional
    public String getLongUrl(String shortCode) {
        return getLongUrl(shortCode, "anonymous");
    }

    @Transactional
    public String getLongUrl(String shortCode, String clientId) {
        if (!cacheService.allowRedirect(shortCode, clientId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }

        String cachedLongUrl = cacheService.getCachedLongUrl(shortCode);
        if (cachedLongUrl != null) {
            incrementClickCount(shortCode);
            cacheService.incrementRedirectAnalytics(shortCode);
            return cachedLongUrl;
        }

        Url url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found"));
        ensureNotExpired(url);
        cacheService.cacheLongUrl(shortCode, url.getLongUrl());
        incrementClickCount(shortCode);
        cacheService.incrementRedirectAnalytics(shortCode);
        return url.getLongUrl();
    }

    @Transactional
    public String getOriginalUrl(String shortCode, String clientId) {
        return getLongUrl(shortCode, clientId);
    }

    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getAnalytics(String shortCode) {
        Url url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found"));
        return mapToAnalytics(url);
    }

    @Transactional(readOnly = true)
    public List<UrlAnalyticsResponse> getAllAnalytics() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Url::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToAnalytics)
                .toList();
    }

    public void updateUrl(String shortCode, String longUrl) {
        Url url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short code not found"));
        ensureNotExpired(url);
        url.setLongUrl(longUrl);
        repository.save(url);
        cacheService.cacheLongUrl(shortCode, longUrl);
    }

    public String createShortUrl(String longUrl) {
        String shortCode = generateShortCode();
        Url mapping = new Url();
        mapping.setShortCode(shortCode);
        mapping.setLongUrl(longUrl);
        mapping.setClickCount(0);
        repository.save(mapping);
        cacheService.cacheLongUrl(shortCode, longUrl);
        return shortCode;
    }

    public void deleteUrl(String shortCode) {
        Url mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Short code not found"));
        repository.delete(mapping);
        cacheService.evictLongUrl(shortCode);
        cacheService.resetAnalytics(shortCode);
    }

    private void incrementClickCount(String shortCode) {
        int updatedRows = repository.incrementClickCount(shortCode);
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found");
        }
    }

    private UrlAnalyticsResponse mapToAnalytics(Url url) {
        long redisClickCount = cacheService.getRedirectAnalytics(url.getShortCode());
        return new UrlAnalyticsResponse(
                url.getShortCode(),
                url.getLongUrl(),
                url.getClickCount(),
                redisClickCount,
                formatDateTime(url.getCreatedAt()),
                formatDateTime(url.getExpiresAt())
        );
    }

    private void ensureNotExpired(Url url) {
        LocalDateTime expiresAt = url.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Short URL has expired");
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_FORMATTER);
    }
}
