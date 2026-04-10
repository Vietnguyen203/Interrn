package com.manager.catalog.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.manager.common.infrastructure.security.JwtAuthenticationFilter;
import com.manager.common.infrastructure.config.JwtAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .authorizeRequests()
                
                // Preflight
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Public APIs
                .antMatchers(HttpMethod.GET, "/foods/**", "/food-categories/**").permitAll()
                .antMatchers("/uploads/**").permitAll()
                
                // Admin only mutations
                .antMatchers(HttpMethod.POST, "/foods/**", "/food-categories/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/foods/**", "/food-categories/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/foods/**", "/food-categories/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
                .and()
                .httpBasic().disable()
                .formLogin().disable();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
