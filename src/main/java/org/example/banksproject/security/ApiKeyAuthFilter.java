package org.example.banksproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyAuthExtractor extractor;

    public ApiKeyAuthFilter(ApiKeyAuthExtractor apiKeyAuthExtractor) {
        this.extractor = apiKeyAuthExtractor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<Authentication> authentication = extractor.extract(request);
        if (authentication.isPresent()) {
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication.get());
        }
        filterChain.doFilter(request, response);
    }
}
