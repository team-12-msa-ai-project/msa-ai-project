package com.team12.api_gateway.config;

import com.team12.api_gateway.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)  // CSRF 비활성화 (필요 시 활성화)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)  // 폼 로그인 비활성화
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)  // HTTP 기본 인증 비활성화
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/users/sign-up").permitAll()
                        .pathMatchers("/api/users/reg/**").hasAuthority("MASTER")
                        .pathMatchers("/api/users/usr").hasAnyAuthority("COMPANY","HUB_MANAGER", "HUB_TO_HUB_DELIVERY", "TO_COMPANY_DELIVERY")
                        .pathMatchers("/api/auth/logout").authenticated()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/client/**").permitAll()
                        .pathMatchers("/api/products/**").permitAll()
                        .pathMatchers("/api/companies/**").permitAll()
                        .pathMatchers("/api/hubs/**").permitAll()
                        .pathMatchers("/api/hub-paths/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/managers/**").hasAuthority("MASTER")
                        .pathMatchers(HttpMethod.PUT, "/api/managers/**").hasAuthority("MASTER")
                        .pathMatchers(HttpMethod.DELETE, "/api/managers/**").hasAuthority("MASTER")
                        .pathMatchers(HttpMethod.GET, "/api/managers/{managerId}").hasAnyAuthority("MASTER","HUB_MANAGER")
                        .pathMatchers(HttpMethod.GET, "/api/managers").hasAnyAuthority("MASTER","HUB_MANAGER")
                        .pathMatchers("/api/managers/hub-to-hub").permitAll()
                        .pathMatchers( "/api/managers/hub-to-company").permitAll()
                        .pathMatchers("/api/managers/**").permitAll()
                        .pathMatchers("/delivery/**").permitAll()
                        .pathMatchers("/route/**").permitAll()
                        .pathMatchers("/api/orders/**").permitAll()
                        .pathMatchers("/slack/**").permitAll()
                        .anyExchange().authenticated()  // 그 외 모든 경로는 인증 필요
                )
                .securityContextRepository(jwtFilter)
                .build();
    }
}
