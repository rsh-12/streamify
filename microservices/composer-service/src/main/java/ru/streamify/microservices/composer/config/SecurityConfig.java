package ru.streamify.microservices.composer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String ACTUATOR = "/actuator/**";
    private static final String API_COMMON = "/api/common/**";

    private static final String SCOPE_WRITE = "SCOPE_song:write";
    private static final String SCOPE_READ = "SCOPE_song:read";

    @Bean
    public SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .pathMatchers("/api/common/stream/**").permitAll() // TODO: remove this line
                .pathMatchers(ACTUATOR).permitAll()
                .pathMatchers(POST, API_COMMON).hasAuthority(SCOPE_WRITE)
                .pathMatchers(DELETE, API_COMMON).hasAnyAuthority(SCOPE_WRITE)
                .pathMatchers(GET, API_COMMON).hasAuthority(SCOPE_READ)
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();

        return http.build();
    }

}
