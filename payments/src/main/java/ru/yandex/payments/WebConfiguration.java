package ru.yandex.payments;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = {"ru.yandex.payments"})
@PropertySource("classpath:application.properties")
public class WebConfiguration {
}
