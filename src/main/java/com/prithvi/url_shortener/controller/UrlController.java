package com.prithvi.url_shortener.controller;
import com.prithvi.url_shortener.service.UrlService;


import com.prithvi.url_shortener.service.UrlService;

import dto.UrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.net.URI;
@RestController
public class UrlController { //class name

    @Autowired
    private UrlService service;

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody UrlRequest longUrl) {
        String shortCode = service.createShortUrl(longUrl.getLongUrl()); //requesting for the url from the service class
        return ResponseEntity.ok(shortCode);

       // return service.shortenUrl(longUrl);
    } //this shortens the url

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) { //takes the shortcode from the url also redirects it to the original url
        String longUrl = service.getOriginalUrl(shortCode); //scans the db and matches the shortcode for the specidifc url

        return ResponseEntity //return the shortcode ex: https://google.com --> 09cebg
                .status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }
    @PutMapping("/{shortcode}")
        public ResponseEntity<String> updateUrl(
            @PathVariable String shortCode, //gets the shortcode from the url
                    @RequestBody UrlRequest request ){
        service.updateUrl(shortCode, request.getLongUrl());
        return ResponseEntity.ok("Updated Successfully");
    }//converts the JSON to object

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode){
        service.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }


}