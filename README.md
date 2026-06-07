# Quiz App

[![CI](https://github.com/pavelchervonenko/web-app-quiz/actions/workflows/main.yml/badge.svg)](https://github.com/pavelchervonenko/web-app-quiz/actions/workflows/main.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pavelchervonenko_web-app-quiz&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pavelchervonenko_web-app-quiz)

Веб-приложение для интерактивных квизов в реальном времени.

## Стек

- Backend: Java 21, Spring Boot 4, Gradle
- Frontend: React, Vite
- Database: PostgreSQL, H2 для dev/test
- Realtime: WebSocket, STOMP
- Deploy: Docker Compose, Nginx

## Локальный Запуск

Backend:

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=development'
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Адреса:

- frontend: http://localhost:5173
- backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Dev organizer:

```text
organizer@example.com
password123
```

## Production-like Запуск

```bash
cp .env.example .env
```

Заполнить `.env`, сгенерировать RSA ключи по инструкции:

```text
deploy/secrets/README.md
```

Запуск:

```bash
docker compose --env-file .env up -d --build
```

## Проверки

Backend:

```bash
cd backend
./gradlew test
```

Frontend:

```bash
cd frontend
npm ci
npm run build
```

## Документация

- [ERD](docs/erd.md)
- [API style](docs/api-style.md)
- [Production checklist](docs/production-checklist.md)
- [VPS deploy](docs/deploy-vps.md)
- [ADR](docs/adr/)
