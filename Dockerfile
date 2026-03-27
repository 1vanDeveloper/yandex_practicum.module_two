# Этап 1: Сборка (Build stage)
# Используем официальный образ Gradle с поддержкой JDK 21
FROM gradle:8.14-jdk21 AS builder

# Устанавливаем рабочую директорию
WORKDIR /build

# Копируем файлы сборки для кэширования зависимостей
COPY build.gradle.kts settings.gradle.kts /build/
COPY src /build/src

# Собираем приложение (пропуская тесты для скорости)
RUN gradle bootJar --no-daemon -x test

# Этап 2: Запуск (Runtime stage)
# Используем легковесный образ JRE 21 для исполнения
FROM eclipse-temurin:21-jre-jammy

# Устанавливаем рабочую директорию для приложения
WORKDIR /app

# Копируем только скомпилированный JAR-файл из этапа сборки
COPY --from=builder /build/build/libs/*.jar application.jar

# Spring Boot 4 по умолчанию работает на порту 8080
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "application.jar"]