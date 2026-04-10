package com.manager.order.infrastructure.config;

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

                // Orders / Tables (Secured)
                .antMatchers("/orders/**", "/tables/**").authenticated()

                // Mutations (Admin only)
                .antMatchers(HttpMethod.POST, "/tables/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/tables/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/tables/**").hasRole("ADMIN")

                .anyRequest().authenticated()
                .and()
                .httpBasic().disable()
                .formLogin().disable();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
