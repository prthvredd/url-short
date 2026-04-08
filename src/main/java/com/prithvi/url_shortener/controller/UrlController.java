package com.prithvi.url_shortener.controller;

import com.prithvi.url_shortener.service.UrlService;
import com.prithvi.url_shortener.dto.UrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {

    private final UrlService service;
    // constructor injection is better than Autowired find and read on that

    public UrlController(UrlService service) {
        this.service = service;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> home() {
        Resource page = new ClassPathResource("static/index.html");
        return ResponseEntity.ok(page);
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody UrlRequest request) {
        String shortCode = service.createShortUrl(request.getLongUrl());
        return ResponseEntity.ok(shortCode);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String longUrl = service.getLongUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", longUrl)
                .build();
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<String> updateUrl(
            @PathVariable String shortCode,
            @RequestBody UrlRequest request) {
        service.updateUrl(shortCode, request.getLongUrl());
        return ResponseEntity.ok("Updated successfully");
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        service.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }
}
