package com.aerolane.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Default chain: form login for the browser UI plus HTTP Basic for the REST API.
     */
    @Bean
    @Profile("!sso")
    public SecurityFilterChain standardFilterChain(HttpSecurity http) throws Exception {
        applyBaseRules(http);
        return http.build();
    }

    /**
     * SSO chain: everything from the default chain plus OIDC login through Keycloak.
     * Keycloak proves who the user is; the local app_users table decides what they
     * are allowed to do (least privilege for unknown identities).
     */
    @Bean
    @Profile("sso")
    public SecurityFilterChain ssoFilterChain(HttpSecurity http,
            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService) throws Exception {
        applyBaseRules(http);
        http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService)));
        return http.build();
    }

    private void applyBaseRules(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/login", "/error", "/actuator/health",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/reports/**").hasAnyRole("AUDITOR", "SUPERVISOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/lanes/**").hasRole("SUPERVISOR")
                        .requestMatchers("/lanes/manage/**").hasRole("SUPERVISOR")
                        .requestMatchers("/inspections/new").hasAnyRole("OFFICER", "SUPERVISOR")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?loggedout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .httpBasic(Customizer.withDefaults())
                // The JSON API is consumed by non-browser clients authenticating per request,
                // so CSRF protection stays on for the UI and is skipped for /api/**.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:"))
                        .frameOptions(frame -> frame.deny()))
                // API clients get a clean 401 instead of a redirect to the login page.
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .invalidSessionUrl("/login?expired"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
