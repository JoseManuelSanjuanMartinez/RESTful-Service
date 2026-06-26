package org.example.banksproject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final ApiKeyAuthFilter authFilter;

    public SecurityConfiguration(ApiKeyAuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.GET, "/banks/", "/banks/{bankId}", "/banks/{bankId}/mortgages/", "/banks/{bankId}/mortgages/{mortgageId}"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST, "/banks/", "/banks/{bankId}/mortgages/"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.PUT, "/banks/{bankId}", "/banks/{bankId}/mortgages/{mortgageId}"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE, "/banks/{bankId}", "/banks/{bankId}/mortgages/{mortgageId}"
                        ).authenticated()
                        .anyRequest().authenticated()
                ).addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
