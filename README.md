# Yandex Practicum Middle Java Developer — Модуль 2

Многомодульный Spring Boot проект, состоящий из двух сервисов:
- **market** — Основной сервис маркетплейса (WebFlux, R2DBC, Redis)
- **payments** — Сервис платежей с OpenAPI документацией

## Технологический стек

- Java 21
- Spring Boot 3.4.4
- Spring WebFlux (reactive)
- Spring Data R2DBC (PostgreSQL)
- Spring Data Redis (reactive)
- OpenAPI 3.0
- Lombok
- Docker & Docker Compose

## Структура проекта

```
.
├── market/          # Сервис маркетплейса (порт 8080)
├── payments/        # Сервис платежей (порт 8081)
├── scripts/         # Скрипты инициализации БД
├── docker-compose.yml
└── Makefile
```

## Быстрый старт

### Запуск в Docker Compose (рекомендуется)

Запуск всех сервисов (приложение, платежи, PostgreSQL, Redis):

```bash
make up-local-infra
# или
docker-compose up --force-recreate --renew-anon-volumes -d
```

Остановка всех сервисов:

```bash
make down-local-infra
# или
docker-compose down --remove-orphans -v
```

### Локальная разработка

#### Сборка всех модулей

```bash
./gradlew build
```

#### Запуск сервиса Market

```bash
./gradlew :market:bootRun
```

Сервис будет доступен по адресу `http://localhost:8080`

#### Запуск сервиса Payments

```bash
./gradlew :payments:bootRun
```

Сервис будет доступен по адресу `http://localhost:8081`

OpenAPI UI: `http://localhost:8081/swagger-ui.html`

## Сборка JAR

Сборка исполняемого JAR для сервиса market:

```bash
./gradlew :market:bootJar
java -jar market/build/libs/market-1.0-SNAPSHOT.jar
```

Сборка исполняемого JAR для сервиса payments:

```bash
./gradlew :payments:bootJar
java -jar payments/build/libs/payments-1.0-SNAPSHOT.jar
```

## Документация API

Сервис payments автоматически генерирует OpenAPI документацию. Сгенерированный `openapi.json` используется сервисом market для создания реактивного WebFlux клиента.

Путь к файлу OpenAPI спецификации:

```
payments/build/openapi.json
```

Для генерации файла выполните команду:

```bash
./gradlew :payments:generateOpenApiDocs
```

## База данных

PostgreSQL инициализируется схемой и тестовыми данными из файлов:
- `scripts/schema.sql` — схема базы данных
- `scripts/data.sql` — тестовые данные

## Проверка работоспособности

- Сервис Market: `http://localhost:8080/actuator/health`
- Сервис Payments: `http://localhost:8081/actuator/health`
