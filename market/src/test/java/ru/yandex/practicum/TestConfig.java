package ru.yandex.practicum;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import ru.yandex.practicum.security.SecurityUser;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter() {
        return mock(ServerOAuth2AuthorizedClientExchangeFilterFunction.class);
    }

    @Bean
    @Primary
    public MapReactiveUserDetailsService testUserDetailsService() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        ru.yandex.practicum.model.User user = new ru.yandex.practicum.model.User();
        user.setId(1L);
        user.setLogin("user");
        user.setPassword(encoder.encode("password"));

        ru.yandex.practicum.model.User user1 = new ru.yandex.practicum.model.User();
        user1.setId(2L);
        user1.setLogin("user1");
        user1.setPassword(encoder.encode("password"));

        ru.yandex.practicum.model.User emptyCartUser = new ru.yandex.practicum.model.User();
        emptyCartUser.setId(3L);
        emptyCartUser.setLogin("empty_cart_user");
        emptyCartUser.setPassword(encoder.encode("password"));

        return new MapReactiveUserDetailsService(
                new SecurityUser(user),
                new SecurityUser(user1),
                new SecurityUser(emptyCartUser)
        );
    }
}
