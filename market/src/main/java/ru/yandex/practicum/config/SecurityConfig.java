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
                        .pathMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .pathMatchers("/login", "/registration", "/logout", "/error").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler((exchange, authentication) -> {
                            exchange.getExchange().getResponse().getHeaders().set("Location", "/items");
                            exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                            return exchange.getExchange().getResponse().setComplete();
                        })
                        .authenticationFailureHandler((exchange, exception) -> {
                            exchange.getExchange().getResponse().getHeaders().set("Location", "/login?error");
                            exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                            return exchange.getExchange().getResponse().setComplete();
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((exchange, authentication) -> 
                            securityContextRepository.save(exchange.getExchange(), null)
                                    .then(Mono.fromRunnable(() -> {
                                        exchange.getExchange().getResponse().getHeaders().set("Location", "/login?logout");
                                        exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                                        exchange.getExchange().getResponse().setComplete();
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
