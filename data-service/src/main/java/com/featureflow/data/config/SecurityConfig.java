package com.featureflow.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                .requestMatchers("/api/v1/products/**").hasAnyRole("ADMIN", "DELIVERY_MANAGER", "PRODUCT_OWNER")
                .requestMatchers("/api/v1/teams/**").hasAnyRole("ADMIN", "DELIVERY_MANAGER", "TEAM_LEAD")
                .requestMatchers("/api/v1/features/**").hasAnyRole("ADMIN", "DELIVERY_MANAGER", "PRODUCT_OWNER")
                .requestMatchers("/api/v1/assignments/**").hasAnyRole("ADMIN", "DELIVERY_MANAGER")
                .requestMatchers("/api/v1/planning-windows/**").hasAnyRole("ADMIN", "DELIVERY_MANAGER")
                .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ADMIN", "DELIVERY_MANAGER", "PRODUCT_OWNER", "TEAM_LEAD")
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
