package dto;

public class UrlAnalyticsResponse {

    private final String shortCode;
    private final String longUrl;
    private final int databaseClickCount;
    private final long redisClickCount;
    private final String createdAt;
    private final String expiresAt;

    public UrlAnalyticsResponse(String shortCode, String longUrl, int databaseClickCount, long redisClickCount,
                                String createdAt, String expiresAt) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.databaseClickCount = databaseClickCount;
        this.redisClickCount = redisClickCount;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public int getDatabaseClickCount() {
        return databaseClickCount;
    }

    public long getRedisClickCount() {
        return redisClickCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
