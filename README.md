# greenhouse-authentication

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Liquibase](https://img.shields.io/badge/Liquibase-migrations-cc6600)
![Docker](https://img.shields.io/badge/Docker-multi--stage-2496ED)

Сервис аутентификации и управления пользователями для системы **[Greenhouse](../../)** — платформы удалённого мониторинга коммерческих теплиц.

Отвечает за регистрацию и вход пользователей (владельцев, монтажников, рабочих) по Telegram ID, хранение ролей и выдачу JWT. Остальные сервисы системы — `greenhouse-inventory` и `greenhouse-telemetries` — проверяют токен локально по общему секрету и обращаются сюда только когда им нужны детали пользователя (email, роль).

> Это один из сервисов super-репозитория **Greenhouse**, подключённый как git submodule. Общая архитектура, docker-compose и инструкция по запуску всей системы — в [README супер-репозитория](../../README.md).

---

## Содержание

- [Роль в системе](#роль-в-системе)
- [Технологии](#технологии)
- [Доменная модель](#доменная-модель)
- [API](#api)
- [Аутентификация и JWT](#аутентификация-и-jwt)
- [Безопасность](#безопасность)
- [Обработка ошибок](#обработка-ошибок)
- [Миграции БД](#миграции-бд)
- [Переменные окружения](#переменные-окружения)
- [Запуск](#запуск)
- [Структура проекта](#структура-проекта)
- [Известные ограничения](#известные-ограничения)

---

## Роль в системе

```
            ┌────────────────────────────┐
  клиент ──▶│  greenhouse-authentication │
            │                             │
            │  POST /auth/sing-up         │
            │  POST /auth/sing-in ────────┼──▶ JWT (role claim, exp 14 дней)
            └──────────────┬─────────────┘
                            │
          GET /api/users/{telegramId}
          POST /api/users/batch
                            │
          ┌─────────────────┴──────────────────┐
          │                                      │
  greenhouse-inventory              greenhouse-telemetries
  (валидирует owner/worker           (резолвит владельца кластера
   при создании кластера              для доступа к телеметрии)
   и назначении рабочих)
```

Сервис — единственный источник правды по пользователям и их ролям. Остальные сервисы доверяют JWT с общим секретом и не обращаются сюда на каждый запрос; Feign-вызовы происходят только при явной бизнес-проверке (например, «имеет ли пользователь роль WORKER перед назначением на кластер»).

## Технологии

- **Java 21**, **Spring Boot 4.0.1**
- Spring Web MVC, Spring Security (метод-level `@PreAuthorize`)
- Spring Data JPA, **PostgreSQL**
- **Liquibase** — миграции схемы (changelog в YAML)
- **java-jwt** (Auth0) — генерация и верификация JWT (HMAC256)
- **BCrypt** — хеширование паролей
- **ModelMapper** — маппинг DTO ↔ Entity
- Lombok, Jakarta Validation
- `spring-dotenv` — подгрузка `.env` в Spring-конфиг
- Docker / multi-stage build: `maven:3.9.5-eclipse-temurin-21` → `eclipse-temurin:21-jre-jammy`

## Доменная модель

### `User`

| Поле | Тип | Описание |
|---|---|---|
| `telegramId` | `bigint` (PK) | Telegram ID пользователя — основной идентификатор во всей системе |
| `email` | `varchar(40)`, unique | Используется при регистрации и входе |
| `password` | `varchar(60)` | BCrypt-хеш, сырой пароль не логируется и не возвращается в ответах |
| `role` | `varchar(20)` | Одно из значений `Role`; новый пользователь получает `ROLE_UNKNOWN` |

### `Role`

```
ROLE_UNKNOWN   — назначается автоматически после регистрации
ROLE_ADMIN     — полный доступ ко всем сервисам системы
ROLE_INSTALLER — регистрирует кластеры и устройства
ROLE_OWNER     — владелец теплицы
ROLE_WORKER    — рабочий, привязанный к кластерам
```

Реальную роль назначает `ADMIN` через `PATCH /api/users/{telegramId}/role` после регистрации.

## API

### `/auth` — публичные эндпоинты

| Метод | Путь | Тело запроса | Ответ |
|---|---|---|---|
| POST | `/auth/sing-up` | `{ telegramId, email, password }` | `{ jwt }` |
| POST | `/auth/sing-in` | `{ telegramId, password }` | `{ jwt }` |

> Опечатка `sing-up` / `sing-in` сохранена намеренно для обратной совместимости — см. [Известные ограничения](#известные-ограничения).

**Регистрация:**

```bash
curl -X POST http://localhost:8080/auth/sing-up \
  -H "Content-Type: application/json" \
  -d '{"telegramId": 123456789, "email": "owner@example.com", "password": "strongPass1"}'
```

**Ответ:**

```json
{ "jwt": "eyJhbGciOiJIUzI1NiJ9..." }
```

Новый пользователь сразу получает валидный JWT с ролью `ROLE_UNKNOWN` — он не даёт доступа ни к одному защищённому ресурсу системы до назначения реальной роли администратором.

### `/api/users` — требуют JWT

| Метод | Путь | Доступ | Описание |
|---|---|---|---|
| GET | `/api/users/{telegramId}` | `ADMIN`, `INSTALLER`, `OWNER` | Получить пользователя по Telegram ID |
| POST | `/api/users/batch` | любой аутентифицированный | Список пользователей по набору ID (межсервисный вызов) |
| PATCH | `/api/users/{telegramId}/role` | `ADMIN` | Назначить роль пользователю |
| DELETE | `/api/users/{telegramId}/remove` | `ADMIN` | Удалить пользователя |

**Назначение роли:**

```bash
curl -X PATCH http://localhost:8080/api/users/123456789/role \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"role": "ROLE_OWNER"}'
```

Поле `role` валидируется по маске `^ROLE_(ADMIN|OWNER|WORKER|INSTALLER|UNKNOWN)$`.

**Батч-запрос (используется другими сервисами для обогащения данных пользователями):**

```bash
curl -X POST http://localhost:8080/api/users/batch \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{"userIds": [123456789, 987654321]}'
```

Ограничение: не более 500 ID за один запрос (`@Size(max = 500)`).

## Аутентификация и JWT

Токены подписываются `HMAC256` общим секретом (`SECURITY_JWT`), одинаковым во всех трёх сервисах системы — это позволяет `greenhouse-inventory` и `greenhouse-telemetries` проверять токены без сетевого запроса к auth-сервису.

**Структура токена:**

| Claim | Значение |
|---|---|
| `iss` | `greenhouse` |
| `sub` | `telegramId` пользователя |
| `token_type` | `USER` |
| `role` | роль пользователя |
| `exp` | выдан + 14 дней |

`JwtAuthenticationProvider` умеет разбирать оба типа токенов — `USER` и `DEVICE` (по claim'у `token_type`), хотя **этот сервис выдаёт только `USER`-токены**. `DEVICE`-токены (TTL 30 минут, привязка к кластеру) выпускает `greenhouse-inventory` после прохождения challenge-response аутентификации.

`JwtFilter` пропускает запрос без заголовка `Authorization` дальше — решение, требовать ли аутентификацию, принимает `SecurityConfiguration`. Если заголовок `Bearer` присутствует, но токен не валиден — возвращает `401` немедленно.

## Безопасность

`SecurityConfiguration`:

- `/auth/**` — публичный путь без аутентификации;
- все остальные запросы — `anyRequest().authenticated()`;
- stateless-сессии, CSRF отключён (REST API за JWT);
- `@EnableMethodSecurity` + `@PreAuthorize` на уровне контроллеров для ролевых ограничений.

Пароли хранятся только как BCrypt-хеш (`BCryptPasswordEncoder`). Сырой пароль нигде не логируется и не попадает в ответы API.

## Обработка ошибок

`GlobalExceptionHandler` приводит все ошибки к единому формату:

```json
{ "statusCode": 409, "message": "User already exists", "timestamp": 1750000000000 }
```

| Исключение | HTTP статус |
|---|---|
| `UserAlreadyExistException` | 409 Conflict |
| `DataIntegrityViolationException` | 409 Conflict |
| `MethodArgumentNotValidException` | 400 Bad Request |
| `HttpMessageNotReadableException` | 400 Bad Request |
| `BadRequestException` | 400 Bad Request |
| `BadCredentialsException` | 401 Unauthorized |
| `EntityNotFoundException` | 404 Not Found |
| `AccessDeniedException` | 403 Forbidden |
| `Exception` (fallback) | 500 Internal Server Error |

## Миграции БД

Liquibase-changelog: `src/main/resources/db/changelog/`:

```
db.changelog-master.yaml
└── migrations/
    └── 001-users-init.yaml   — создание таблицы users
```

Применяются автоматически при старте приложения.

## Переменные окружения

| Переменная | Назначение |
|---|---|
| `AUTHENTICATION_SERVER_PORT` | Порт сервиса (пример: `8080`) |
| `AUTHENTICATION_SPRING_DATASOURCE_URL` | JDBC URL базы `greenhouse_authorization` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Креды Postgres |
| `SECURITY_JWT` | Общий секрет для подписи JWT — **одинаковый во всех сервисах системы** |

> ⚠️ Не коммитьте `.env` с реальными значениями `SECURITY_JWT` и паролями. В репозитории должен лежать только `.env.example` с плейсхолдерами.

## Запуск

### В составе всей системы (рекомендуется)

```bash
cd Greenhouse
docker compose up -d --build
```

### Локально, для разработки

Нужен локальный Postgres с базой `greenhouse_authorization` и заполненные переменные окружения.

```bash
./mvnw clean package -DskipTests
java -jar target/greenhouse-0.0.1-SNAPSHOT.jar
```

или:

```bash
./mvnw spring-boot:run
```

### Docker-образ отдельно

```bash
docker build -t greenhouse-authentication .
docker run --rm -p 8080:8080 --env-file .env greenhouse-authentication
```

## Структура проекта

```
src/main/java/com/example/greenhouse/
├── controllers/
│   ├── AuthController.java              # /auth/sing-up, /auth/sing-in
│   └── UserController.java               # /api/users/**
├── services/
│   ├── UserService.java                   # бизнес-логика: регистрация, вход, управление
│   └── CustomUserDetailsService.java       # интеграция с Spring Security
├── store/
│   └── UserStore.java                      # инкапсуляция доступа к UserRepository
├── repositories/postgres/
│   └── UserRepository.java                  # Spring Data JPA
├── models/
│   └── User.java                             # JPA-entity
├── DTO/
│   ├── auth/                                  # SingUpDTO, SingInDTO, SuccessfullyAuthenticatedDTO
│   ├── user/                                   # UserInfoDTO, AssignRoleToPersonDTO, UserInfoBatchRequestDTO
│   └── error/                                   # ErrorResponseDTO
├── security/
│   ├── jwt/                                     # JwtUtil, JwtFilter, JwtAuthenticationProvider
│   ├── UserPrincipal.java
│   └── DevicePrincipal.java
├── configurations/
│   ├── security/SecurityConfiguration.java
│   └── general/BeansConfiguration.java          # ModelMapper, BCryptPasswordEncoder
├── exceptions/                                  # GlobalExceptionHandler + кастомные исключения
└── util/
    ├── Convertor.java
    └── enums/                                   # Role, TokenType
```
