package org.example.banksproject.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("API_USER")
public class ApiUser {
    @Id
    private Integer id;
    private String username;
    private String apiKey;

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getApiKey() {
        return apiKey;
    }
}
