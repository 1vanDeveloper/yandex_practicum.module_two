package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class LoginController {

    @GetMapping("/login")
    public Mono<Rendering> loginPage() {
        return Mono.just(Rendering.view("login").build());
    }

    @GetMapping("/logout")
    public Mono<Rendering> logoutPage() {
        return Mono.just(Rendering.view("logout").build());
    }
}
