package ru.yandex.practicum;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSecurityUserSecurityContextFactory.class)
public @interface WithMockSecurityUser {
    String username() default "user";
    long userId() default 1L;
}
