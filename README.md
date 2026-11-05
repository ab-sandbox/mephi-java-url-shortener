## URL Shortener

Сервис сокращения URL с ограничением количества переходов, временем жизни ссылок и системой уведомлений.

Проект разработан на Java по кейсу компании **PROMO IT** в рамках образовательной программы НИЯУ МИФИ. Реализован как
клиент-серверное приложение с REST API, базой данных PostgreSQL и отдельным консольным клиентом.

### Возможности

- Создание коротких ссылок с уникальными кодами.
- Ограничение количества переходов по ссылке.
- Автоматическое истечение срока действия ссылок.
- Учёт переходов с защитой от превышения лимита при конкурентных запросах.
- Идентификация пользователей по UUID.
- Просмотр, изменение и удаление собственных ссылок.
- Уведомления об истечении срока действия и достижении лимита переходов.
- Автоматическая очистка просроченных ссылок.
- Консольный клиент с командным и интерактивным режимами.

### Технологии

| Компонент      | Технологии                                       |
|----------------|--------------------------------------------------|
| Backend        | Java 21, Spring Boot, Spring Data JPA, Hibernate |
| База данных    | PostgreSQL 16, Flyway                            |
| CLI            | Java 21, picocli, Java HttpClient, Jackson       |
| Инфраструктура | Docker Compose, Maven                            |
| Тестирование   | JUnit 5, Spring Boot Test, Testcontainers        |

### Структура проекта

```text
url-shortener/
├── backend/       # REST API и бизнес-логика
├── cli/           # Консольный клиент
├── pom.xml        # Корневой Maven-проект
└── README.md
```

Backend и CLI реализованы как независимые Maven-модули.

### Запуск

#### Требования

- JDK 21
- Docker и Docker Compose
- Maven Wrapper (включён в проект)

#### Сборка

```bash
./mvnw clean verify
```

Команда собирает модули и запускает тесты.

#### Backend

Для работы сервиса необходимо запустить PostgreSQL через Docker Compose, затем backend-приложение.

```bash
./mvnw -pl backend spring-boot:run
```

Backend по умолчанию доступен по адресу `http://localhost:8080`.

#### CLI

Консольный клиент собирается в исполняемый JAR:

```bash
./mvnw -pl cli package
```

Интерактивный режим:

```bash
java -jar cli/target/url-shortener-cli.jar
```

Командный режим:

```bash
java -jar cli/target/url-shortener-cli.jar --help
```

Примеры команд:

```bash
# Создать короткую ссылку
java -jar cli/target/url-shortener-cli.jar create \
    --url https://example.com --limit 10

# Просмотреть свои ссылки
java -jar cli/target/url-shortener-cli.jar list

# Открыть ссылку в браузере
java -jar cli/target/url-shortener-cli.jar open CODE

# Изменить лимит переходов
java -jar cli/target/url-shortener-cli.jar update CODE --limit 20

# Удалить ссылку
java -jar cli/target/url-shortener-cli.jar delete CODE

# Просмотреть непрочитанные уведомления
java -jar cli/target/url-shortener-cli.jar notifications unread
```

При первом создании ссылки CLI сохраняет UUID пользователя в `~/.url-shortener/session.properties` и использует его для
последующих запросов.

Адрес backend можно переопределить через переменную окружения:

```bash
URL_SHORTENER_API_URL=http://localhost:8080 \
java -jar cli/target/url-shortener-cli.jar list
```

### REST API

| Метод  | Endpoint                       | Описание                             |
|--------|--------------------------------|--------------------------------------|
| POST   | `/api/links`                   | Создание ссылки                      |
| GET    | `/api/links`                   | Список ссылок пользователя           |
| PATCH  | `/api/links/{code}`            | Изменение лимита переходов           |
| DELETE | `/api/links/{code}`            | Удаление ссылки                      |
| GET    | `/{code}`                      | Переход по короткой ссылке           |
| GET    | `/api/notifications`           | Список уведомлений                   |
| GET    | `/api/notifications/unread`    | Непрочитанные уведомления            |
| PATCH  | `/api/notifications/{id}/read` | Отметка уведомления как прочитанного |

Для пользовательских операций используется заголовок `X-User-Id`.

### Тестирование

Проект включает модульные и интеграционные тесты, в том числе проверку конкурентных переходов по ссылке с ограниченным
количеством использований.

Запуск всех тестов:

```bash
./mvnw test
```

### О проекте

Проект основан на практическом кейсе **PROMO IT**, предложенном в рамках обучения в НИЯУ МИФИ. Архитектура и реализация
развиваются как самостоятельное приложение с несколькими клиентскими интерфейсами.
