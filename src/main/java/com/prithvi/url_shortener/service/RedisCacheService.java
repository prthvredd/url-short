package com.prithvi.url_shortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    private static final Duration URL_CACHE_TTL = Duration.ofHours(24);
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int RATE_LIMIT_MAX_REQUESTS = 10;
    private static final String URL_CACHE_KEY_PREFIX = "url:";
    private static final String ANALYTICS_KEY_PREFIX = "analytics:";
    private static final String RATE_LIMIT_KEY_PREFIX = "rate-limit:";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getCachedLongUrl(String shortCode) {
        try {
            return redisTemplate.opsForValue().get(urlCacheKey(shortCode));
        } catch (DataAccessException ex) {
            log.warn("Redis read failed for short code {}", shortCode, ex);
            return null;
        }
    }

    public void cacheLongUrl(String shortCode, String longUrl) {
        try {
            redisTemplate.opsForValue().set(urlCacheKey(shortCode), longUrl, URL_CACHE_TTL);
        } catch (DataAccessException ex) {
            log.warn("Redis write failed for short code {}", shortCode, ex);
        }
    }

    public void evictLongUrl(String shortCode) {
        try {
            redisTemplate.delete(urlCacheKey(shortCode));
        } catch (DataAccessException ex) {
            log.warn("Redis eviction failed for short code {}", shortCode, ex);
        }
    }

    public boolean allowRedirect(String shortCode, String clientId) {
        try {
            String key = rateLimitKey(shortCode, clientId);
            Long attempts = redisTemplate.opsForValue().increment(key);
            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(key, RATE_LIMIT_WINDOW);
            }
            return attempts == null || attempts <= RATE_LIMIT_MAX_REQUESTS;
        } catch (DataAccessException ex) {
            log.warn("Redis rate limit check failed for short code {} and client {}", shortCode, clientId, ex);
            return true;
        }
    }

    public long incrementRedirectAnalytics(String shortCode) {
        try {
            Long redirects = redisTemplate.opsForValue().increment(analyticsKey(shortCode));
            return redirects == null ? 0L : redirects;
        } catch (DataAccessException ex) {
            log.warn("Redis analytics increment failed for short code {}", shortCode, ex);
            return 0L;
        }
    }

    public long getRedirectAnalytics(String shortCode) {
        try {
            String value = redisTemplate.opsForValue().get(analyticsKey(shortCode));
            return value == null ? 0L : Long.parseLong(value);
        } catch (DataAccessException | NumberFormatException ex) {
            log.warn("Redis analytics read failed for short code {}", shortCode, ex);
            return 0L;
        }
    }

    public void resetAnalytics(String shortCode) {
        try {
            redisTemplate.delete(analyticsKey(shortCode));
        } catch (DataAccessException ex) {
            log.warn("Redis analytics reset failed for short code {}", shortCode, ex);
        }
    }

    private String urlCacheKey(String shortCode) {
        return URL_CACHE_KEY_PREFIX + shortCode;
    }

    private String analyticsKey(String shortCode) {
        return ANALYTICS_KEY_PREFIX + shortCode;
    }

    private String rateLimitKey(String shortCode, String clientId) {
        return RATE_LIMIT_KEY_PREFIX + shortCode + ":" + clientId;
    }
}
