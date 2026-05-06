package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.repository.UserRepository;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("registrationForm") RegistrationForm form) {
        return "registration";
    }

    @PostMapping("/registration")
    public Mono<String> register(@ModelAttribute("registrationForm") RegistrationForm form,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            return Mono.just("registration");
        }

        return userRepository.findByLogin(form.getLogin())
                .flatMap(existingUser -> {
                    model.addAttribute("error", "User with this login already exists");
                    return Mono.just("registration");
                })
                .switchIfEmpty(Mono.defer(() -> {
                    User user = new User();
                    user.setLogin(form.getLogin());
                    user.setPassword(passwordEncoder.encode(form.getPassword()));
                    return userRepository.save(user)
                            .then(Mono.just("redirect:/login?registered"));
                }));
    }

    @Data
    public static class RegistrationForm {
        @NotBlank(message = "Login is required")
        @Size(min = 3, max = 256, message = "Login must be between 3 and 256 characters")
        private String login;

        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 256, message = "Password must be between 4 and 256 characters")
        private String password;
    }
}
