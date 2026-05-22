package com.vietnl.catalogservice.infrastructure.security;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // GET categories & items: công khai (menu nhà hàng không cần đăng nhập để xem)
                        .requestMatchers(HttpMethod.GET, "/catalog-service/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/catalog-service/items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/catalog-service/uploads/**").permitAll()

                        // Bếp được phép đề xuất món mới và đề xuất sửa công thức
                        .requestMatchers(HttpMethod.POST, "/catalog-service/items/propose").authenticated()
                        .requestMatchers(HttpMethod.POST, "/catalog-service/items/upload").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/catalog-service/items/*/propose-recipe").hasAnyRole("KITCHEN", "CHEF", "ADMIN")

                        // Chỉ ADMIN mới được duyệt/từ chối/tạo/sửa/xóa
                        .requestMatchers(HttpMethod.POST, "/catalog-service/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/catalog-service/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/catalog-service/**").hasRole("ADMIN")

                        // Còn lại yêu cầu đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
