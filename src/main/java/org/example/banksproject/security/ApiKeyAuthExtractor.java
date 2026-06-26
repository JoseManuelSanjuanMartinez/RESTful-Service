package org.example.banksproject.security;

import jakarta.servlet.http.HttpServletRequest;
import org.example.banksproject.models.ApiUser;
import org.example.banksproject.repositories.ApiUsersRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApiKeyAuthExtractor {

    private final ApiUsersRepository apiUsersRepository;

    public ApiKeyAuthExtractor(ApiUsersRepository apiUsersRepository) {
        this.apiUsersRepository = apiUsersRepository;
    }

    public Optional<Authentication> extract(HttpServletRequest request) {
        String providedKey = request.getHeader("ApiKey");

        if (providedKey == null) return Optional.empty();

        Optional<ApiUser> found = apiUsersRepository.findByApiKey(providedKey);
        if (found.isEmpty()) return Optional.empty();

        return Optional.of(new ApiKeyAuth(AuthorityUtils.NO_AUTHORITIES, found.get(), providedKey));
    }
}
