package com.prithvi.url_shortener.service;
// Correct package (must match folder)

import com.prithvi.url_shortener.model.Url;
// Import entity

import com.prithvi.url_shortener.repository.UrlRepository;
// Import repository

import org.springframework.beans.factory.annotation.Autowired;
// Dependency injection

import org.springframework.stereotype.Service;
// Marks this as service layer

import java.util.UUID;
// For generating short codes

@Service
public class UrlService {

    @Autowired
    private UrlRepository repository;
    // Spring injects repository here
    private String generateShortCode(){
        return UUID.randomUUID().toString().substring(0,6);
    }

    public String shortenUrl(String longUrl) {
        String shortCode = UUID.randomUUID().toString().substring(0, 8);
        // Generate random short code

        Url url = new Url();
        // Create object

        url.setShortCode(shortCode);
        url.setLongUrl(longUrl);

        repository.save(url);
        // Save to DB

        return shortCode;

    }

    public String getOriginalUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(Url::getLongUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }

    public void updateUrl(String shortCode, String longUrl){
        Url url = repository.findByShortCode(shortCode).orElseThrow(() -> new RuntimeException("Short Code not Found"));
        url.setLongUrl(longUrl);
        repository.save(url);
    }
    public String createShortUrl(String longUrl){
        String shortCode = generateShortCode();
        Url mapping = new Url();
        mapping.setShortCode(shortCode);
        mapping.getLongUrl(longUrl);
        repository.save(mapping);
        return shortCode;
    }
    public String deleteUrl(String shortCode){
        Url mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Not found"));
                repository.delete(mapping);

        return shortCode;
    }
}