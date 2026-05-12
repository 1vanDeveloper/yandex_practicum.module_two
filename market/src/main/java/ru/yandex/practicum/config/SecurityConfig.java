package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.CustomUserDetailsService;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder());
        return manager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ServerRequestCache requestCache,
                                                         ServerSecurityContextRepository securityContextRepository) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public resources - accessible to anonymous users
                        .pathMatchers(HttpMethod.GET, "/").permitAll()
                        .pathMatchers(HttpMethod.GET, "/items").permitAll()
                        .pathMatchers(HttpMethod.GET, "/items/{id}").permitAll()
                        .pathMatchers(HttpMethod.GET, "/images/**").permitAll()
                        // Authentication endpoints
                        .pathMatchers("/login", "/registration", "/logout", "/error").permitAll()
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                .exceptionHandling(spec -> spec
                        .accessDeniedHandler((webExchange, accessDeniedException) -> {
                            webExchange.getResponse().getHeaders().set("Location", "/login?accessDenied");
                            webExchange.getResponse().setStatusCode(HttpStatus.FOUND);
                            return webExchange.getResponse().setComplete();
                        })
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            webFilterExchange.getExchange().getResponse().getHeaders().set("Location", "/items");
                            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                            return webFilterExchange.getExchange().getResponse().setComplete();
                        })
                        .authenticationFailureHandler((webFilterExchange, exception) -> {
                            webFilterExchange.getExchange().getResponse().getHeaders().set("Location", "/login?error");
                            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                            return webFilterExchange.getExchange().getResponse().setComplete();
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((webFilterExchange, authentication) ->
                            securityContextRepository.save(webFilterExchange.getExchange(), null)
                                    .then(Mono.fromRunnable(() -> {
                                        webFilterExchange.getExchange().getResponse().getHeaders().set("Location", "/login?logout");
                                        webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                                        webFilterExchange.getExchange().getResponse().setComplete();
                                    }))
                        )
                )
                .securityContextRepository(securityContextRepository)
                .requestCache(spec -> spec.requestCache(requestCache));

        return http.build();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public ServerRequestCache requestCache() {
        return new WebSessionServerRequestCache();
    }
}
