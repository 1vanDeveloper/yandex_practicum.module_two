package ru.yandex.practicum;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.yandex.practicum.config.SecurityConfig;
import ru.yandex.practicum.security.SecurityUser;

@TestConfiguration
@Import({WebConfiguration.class, SecurityConfig.class})
public class TestConfig {

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
