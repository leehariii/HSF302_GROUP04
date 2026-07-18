package com.fptu.forum.config;

import com.fptu.forum.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cau hinh Spring Security chinh:
 * - Phan quyen URL theo role
 * - Custom login/logout form
 * - BCrypt password encoder
 * - Ket noi voi CustomUserDetailsService
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // ----------------------------------------------------------------
    // Password Encoder: BCrypt
    // ----------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ----------------------------------------------------------------
    // Authentication Provider: dung DB + BCrypt
    // ----------------------------------------------------------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ----------------------------------------------------------------
    // AuthenticationManager (inject vao AuthService)
    // ----------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Ghi chu: Spring Security 6 tu dong pick up RoleHierarchy @Bean
    // va ap dung vao ca URL authorization lan Thymeleaf sec:authorize.
    // Khong can khai bao webSecurityExpressionHandler thu cong nua.

    // ----------------------------------------------------------------
    // Security Filter Chain: cau hinh phan quyen URL
    // ----------------------------------------------------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Public: trang auth, static files
                .requestMatchers(
                    "/auth/login", "/auth/register", "/auth/forgot-password",
                    "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/error/**"
                ).permitAll()

                // Public: xem bai viet, topic (cho phep guest xem)
                .requestMatchers("/", "/home", "/topics", "/topics/**",
                    "/posts/{id:[0-9]+}").permitAll()

                // Admin only
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Moderator tro len (MODERATOR + ADMIN nho role hierarchy)
                .requestMatchers("/moderator/**").hasRole("MODERATOR")

                // Dang nhap moi duoc dung cac chuc nang nay
                .requestMatchers(
                    "/posts/new", "/posts/create",
                    "/posts/*/edit", "/posts/*/update",
                    "/posts/*/delete",
                    "/comments/**",
                    "/likes/**",
                    "/member/saved-posts*",
                    "/reports/**",
                    "/profile", "/change-password"
                ).authenticated()

                // Mac dinh: con lai yeu cau dang nhap
                .anyRequest().authenticated()
            )

            // Custom login form
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )

            // Custom logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Xu ly loi: 403 Forbidden
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
