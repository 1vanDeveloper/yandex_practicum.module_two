package ru.yandex.practicum;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@ComponentScan(basePackages = {"ru.yandex.practicum"})
@PropertySource("classpath:application.properties")
public class WebConfiguration {
}
