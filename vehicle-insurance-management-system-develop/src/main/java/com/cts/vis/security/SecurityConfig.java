package com.cts.vis.security;

import com.cts.vis.model.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Checks if the authenticated user has a specific role.
     */
    private boolean isRole(Authentication auth, UserRole role) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority.getAuthority().equals(role.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Log out users who attempt to access the wrong portal (e.g., Admin trying to log into Customer portal).
     */
    private void handleRoleMismatch(HttpServletRequest req, HttpServletResponse res, Authentication auth, String redirectUrl) throws IOException {
        new SecurityContextLogoutHandler().logout(req, res, auth);
        res.sendRedirect(redirectUrl);
    }

    // ======================= ADMIN SECURITY CHAIN =======================
    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**");

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .anyRequest().hasRole("ADMIN")
        );

        http.formLogin(login -> login
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        if (isRole(authentication, UserRole.ROLE_ADMIN)) {
                            response.sendRedirect("/admin/dashboard");
                        } else {
                            handleRoleMismatch(request, response, authentication, "/admin/login?error=onlyAdmin");
                        }
                    }
                })
                // Triggered for unregistered admins or wrong passwords
                .failureUrl("/admin/login?error=true")
        );

        http.logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
        );

        return http.build();
    }

    // ======================= CUSTOMER SECURITY CHAIN =======================
    @Bean
    @Order(2)
    public SecurityFilterChain customerChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**");

        // Disable CSRF for registration endpoint as per your previous requirement
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/customer/register"));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/customer/login", "/customer/register", "/css/**", "/images/**", "/js/**").permitAll()
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .anyRequest().authenticated()
        );

        http.formLogin(login -> login
                .loginPage("/customer/login")
                .loginProcessingUrl("/customer/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        if (isRole(authentication, UserRole.ROLE_CUSTOMER)) {
                            response.sendRedirect("/customer/dashboard");
                        } else {
                            // If an Admin accidentally logs in here, kick them out and show error
                            handleRoleMismatch(request, response, authentication, "/customer/login?error=onlyCustomer");
                        }
                    }
                })
                //  CRITICAL: This handles unregistered customers and wrong passwords.
                // It appends ?error=true to the URL, which your HTML alert will detect.
                .failureUrl("/customer/login?error=true")
        );

        http.logout(logout -> logout
                .logoutUrl("/customer/logout")
                .logoutSuccessUrl("/customer/login?logout=true")
        );

        // Tell Spring Security to use your custom DB service for looking up users
        http.userDetailsService(userDetailsService);

        return http.build();
    }
}