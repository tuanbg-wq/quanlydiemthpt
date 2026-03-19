package com.quanly.webdiem.config;

import com.quanly.webdiem.security.PasswordHasher;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public PasswordEncoder passwordEncoder(PasswordHasher passwordHasher) {
        return passwordHasher;
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
            String ctx = req.getContextPath();

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
            boolean isTeacher = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_Giao_vien"));

            if (isAdmin) {
                res.sendRedirect(ctx + "/admin/dashboard");
            } else if (isTeacher) {
                res.sendRedirect(ctx + "/teacher/dashboard");
            } else {
                res.sendRedirect(ctx + "/home");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_Admin")
                        .requestMatchers("/teacher/**").hasAnyAuthority("ROLE_Admin", "ROLE_Giao_vien")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expired=true")
                );

        return http.build();
    }
}
