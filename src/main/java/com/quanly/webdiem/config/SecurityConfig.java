package com.quanly.webdiem.config;

import com.quanly.webdiem.model.service.teacher.TeacherHomeroomScopeService;
import com.quanly.webdiem.security.PasswordHasher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.userdetails.UserDetailsService;

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
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler successHandler(TeacherHomeroomScopeService homeroomScopeService) {
        return (HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
            String ctx = req.getContextPath();
            String username = auth == null ? null : auth.getName();

            boolean isAdmin = hasAnyAuthority(auth, "ROLE_Admin", "ROLE_ADMIN");
            boolean isHomeroomTeacherByAuthority = hasAnyAuthority(auth, "ROLE_GVCN");
            boolean isSubjectTeacher = hasAnyAuthority(auth, "ROLE_GVBM", "ROLE_Giao_vien");
            boolean hasHomeroomClass = false;

            if (!isAdmin && username != null && (isHomeroomTeacherByAuthority || isSubjectTeacher)) {
                try {
                    TeacherHomeroomScopeService.TeacherHomeroomScope scope = homeroomScopeService.resolveByUsername(username);
                    hasHomeroomClass = scope != null && scope.hasHomeroomClass();
                } catch (Exception ignored) {
                    hasHomeroomClass = false;
                }
            }

            if (isAdmin) {
                res.sendRedirect(ctx + "/admin/dashboard");
            } else if (isHomeroomTeacherByAuthority || hasHomeroomClass) {
                res.sendRedirect(ctx + "/teacher/dashboard");
            } else if (isSubjectTeacher) {
                res.sendRedirect(ctx + "/teacher-subject/dashboard");
            } else {
                res.sendRedirect(ctx + "/home");
            }
        };
    }

    private boolean hasAnyAuthority(Authentication auth, String... expectedAuthorities) {
        if (auth == null || auth.getAuthorities() == null || expectedAuthorities == null || expectedAuthorities.length == 0) {
            return false;
        }

        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            String actual = authority.getAuthority();
            for (String expected : expectedAuthorities) {
                if (expected != null && actual.equalsIgnoreCase(expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider authenticationProvider,
                                           AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_Admin")
                        .requestMatchers("/teacher-subject/**").hasAnyAuthority("ROLE_Admin", "ROLE_Giao_vien", "ROLE_GVCN", "ROLE_GVBM")
                        .requestMatchers("/teacher/**").hasAnyAuthority("ROLE_Admin", "ROLE_Giao_vien", "ROLE_GVCN", "ROLE_GVBM")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .invalidSessionStrategy((request, response) -> {
                            String contextPath = request.getContextPath();
                            String loginPath = contextPath + "/login";
                            String requestPath = request.getRequestURI();

                            Cookie clearSessionCookie = new Cookie("JSESSIONID", "");
                            clearSessionCookie.setMaxAge(0);
                            clearSessionCookie.setPath(contextPath == null || contextPath.isBlank() ? "/" : contextPath);
                            clearSessionCookie.setHttpOnly(true);
                            response.addCookie(clearSessionCookie);

                            if (loginPath.equals(requestPath)) {
                                response.sendRedirect(loginPath);
                            } else {
                                response.sendRedirect(loginPath + "?expired=true");
                            }
                        })
                );

        return http.build();
    }
}
