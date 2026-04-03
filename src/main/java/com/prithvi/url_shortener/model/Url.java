package com.prithvi.url_shortener.model;

import jakarta.persistence.*;

@Entity
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//creates the id for each object automatically
    private Long id;

    private String shortCode;
    private String longUrl;

    // getters
    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    // setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
}