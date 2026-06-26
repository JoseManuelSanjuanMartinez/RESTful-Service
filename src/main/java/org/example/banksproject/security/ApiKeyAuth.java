package org.example.banksproject.security;

import org.example.banksproject.models.ApiUser;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApiKeyAuth extends AbstractAuthenticationToken {
    private final ApiUser principal;
    private final String apiKey;

    public ApiKeyAuth(Collection<? extends GrantedAuthority> authorities, ApiUser principal, String apiKey) {
        super(authorities);
        this.principal = principal;
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public @Nullable String getCredentials() {
        return apiKey;
    }

    @Override
    public @Nullable ApiUser getPrincipal() {
        return principal;
    }
}
