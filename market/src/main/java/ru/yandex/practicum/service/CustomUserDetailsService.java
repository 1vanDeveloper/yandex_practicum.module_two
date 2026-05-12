package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.security.SecurityUser;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByLogin(username)
                .<UserDetails>map(SecurityUser::new)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }
}
