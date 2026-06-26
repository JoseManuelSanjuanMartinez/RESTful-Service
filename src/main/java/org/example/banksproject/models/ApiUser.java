package org.example.banksproject.models;

import org.springframework.data.annotation.Id;

public class ApiUser {
    @Id
    private int id;
    private String username;
    private String apiKey;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getApiKey() {
        return apiKey;
    }
}
