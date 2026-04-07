package com.prithvi.url_shortener.config;

import com.prithvi.url_shortener.service.UrlService;
import dto.UrlAnalyticsResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class AnalyticsApiConfig {

    @Bean
    public ServletRegistrationBean<HttpServlet> analyticsServlet(UrlService service) {
        HttpServlet servlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                String path = request.getPathInfo();
                if (path == null || "/".equals(path) || path.isBlank()) {
                    writeJson(response, buildDashboardPayload(service.getAllAnalytics()));
                    return;
                }

                String shortCode = path.startsWith("/") ? path.substring(1) : path;
                writeJson(response, toJson(service.getAnalytics(shortCode)));
            }

            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
                String path = request.getPathInfo();
                if ("/query".equals(path) || path == null || "/".equals(path)) {
                    writeJson(response, buildDashboardPayload(service.getAllAnalytics()));
                    return;
                }

                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        };

        ServletRegistrationBean<HttpServlet> registration = new ServletRegistrationBean<>(servlet, "/api/analytics/*");
        registration.setName("analyticsServlet");
        registration.addInitParameter("supportedMethods", HttpMethod.GET.name() + "," + HttpMethod.POST.name());
        return registration;
    }

    private String buildDashboardPayload(List<UrlAnalyticsResponse> analytics) {
        return analytics.stream()
                .map(this::toJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String toJson(UrlAnalyticsResponse response) {
        return "{"
                + "\"shortCode\":\"" + escapeJson(response.getShortCode()) + "\","
                + "\"longUrl\":\"" + escapeJson(response.getLongUrl()) + "\","
                + "\"databaseClickCount\":" + response.getDatabaseClickCount() + ","
                + "\"redisClickCount\":" + response.getRedisClickCount() + ","
                + "\"createdAt\":" + quoteOrNull(response.getCreatedAt()) + ","
                + "\"expiresAt\":" + quoteOrNull(response.getExpiresAt())
                + "}";
    }

    private String quoteOrNull(String value) {
        return value == null ? "null" : "\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private void writeJson(HttpServletResponse response, String payload) throws IOException {
        byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
        response.getOutputStream().flush();
    }
}
