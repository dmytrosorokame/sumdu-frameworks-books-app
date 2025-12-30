package sumdu.edu.ua.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources - permit all
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // H2 Console - permit all (for development)
                .requestMatchers("/h2-console/**").permitAll()
                // Login and registration - permit all
                .requestMatchers("/login", "/register", "/confirm").permitAll()
                // Error pages - permit all
                .requestMatchers("/error", "/403", "/404").permitAll()
                // API endpoints - require authentication, ADMIN for modifications
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                .requestMatchers("/api/**").hasRole("ADMIN")
                // Books viewing - USER and ADMIN
                .requestMatchers(HttpMethod.GET, "/books").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/books/{id}").hasAnyRole("USER", "ADMIN")
                // Comments on books - authenticated users can add comments via POST /books/{id}
                .requestMatchers(HttpMethod.POST, "/books/{id}").authenticated()
                // Books modification - ADMIN only
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/books/add").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("ADMIN")
                // Comments viewing - USER and ADMIN
                .requestMatchers(HttpMethod.GET, "/comments/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/users/*/comments").hasAnyRole("USER", "ADMIN")
                // Comments modification - authenticated users
                .requestMatchers(HttpMethod.POST, "/comments/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/books", true)
                .failureUrl("/login?error=true")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            )
            // Disable CSRF for H2 Console and API endpoints
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/**", "/comments/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            log.info("User '{}' logged in successfully with roles: {}",
                    authentication.getName(),
                    authentication.getAuthorities());
            response.sendRedirect("/books");
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            log.warn("Failed login attempt: {}", exception.getMessage());
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                log.info("User '{}' logged out successfully", authentication.getName());
            } else {
                log.info("User logged out (session expired or already logged out)");
            }
            response.sendRedirect("/login?logout=true");
        };
    }
}

