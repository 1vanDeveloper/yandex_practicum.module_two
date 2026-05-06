package ru.yandex.practicum;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.security.SecurityUser;

public class WithMockSecurityUserSecurityContextFactory implements WithSecurityContextFactory<WithMockSecurityUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockSecurityUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = new User();
        user.setId(annotation.userId());
        user.setLogin(annotation.username());
        user.setPassword("password");

        SecurityUser securityUser = new SecurityUser(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, "password", securityUser.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}
