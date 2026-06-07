# Production Checklist

Этот checklist нужен перед деплоем Web App Quiz на сервер.
Он фиксирует, что должно быть настроено, какие значения нельзя хранить в git и как проверить, что production-сборка действительно работает.

## 1. Профили Приложения

В production backend должен запускаться с отдельным профилем:

```env
SPRING_PROFILES_ACTIVE=production
```

Проверить:

- не используется `development`;
- не запускается dev seed;
- приложение подключается к PostgreSQL, а не к H2;
- schema управляется Flyway migrations.

## 2. PostgreSQL

Нужны значения:

```env
POSTGRES_DB=quiz
POSTGRES_USER=quiz
POSTGRES_PASSWORD=change-me
```

Для backend:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/quiz
SPRING_DATASOURCE_USERNAME=quiz
SPRING_DATASOURCE_PASSWORD=change-me
```

Проверить:

- контейнер PostgreSQL запущен;
- backend может подключиться к БД;
- Flyway применил миграции;
- приложение не использует `ddl-auto=create` или `ddl-auto=update` в production.

## 3. Flyway

В production схема БД должна создаваться миграциями.

Проверить:

- миграции лежат в `backend/src/main/resources/db/migration`;
- названия файлов идут по формату `V1__description.sql`;
- backend стартует на пустой PostgreSQL базе;
- таблица `flyway_schema_history` появляется после старта;
- повторный старт приложения не ломает схему.

## 4. JWT И RSA Ключи

JWT подписывается private key, а проверяется public key.
В production ключи нельзя хранить в git.

Нужны значения:

```env
RSA_PRIVATE_KEY=file:/run/secrets/jwt-private.pem
RSA_PUBLIC_KEY=file:/run/secrets/jwt-public.pem
```

Для Docker Compose также нужны пути к файлам на host-машине:

```env
JWT_PRIVATE_KEY_PATH=./deploy/secrets/jwt-private.pem
JWT_PUBLIC_KEY_PATH=./deploy/secrets/jwt-public.pem
```

Проверить:

- private key доступен только backend-контейнеру;
- public key доступен backend-контейнеру;
- приложение стартует без `FileNotFoundException`;
- login/register возвращают JWT;
- приватный endpoint принимает JWT.

Минимальная проверка:

```bash
curl -i -X POST https://your-domain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "organizer@example.com",
    "password": "DEV_ORGANIZER_PASSWORD"
  }'
```

## 5. Frontend Build

Frontend собирается через Vite:

```bash
cd frontend
npm ci
npm run build
```

Результат:

```text
frontend/dist
```

Проверить:

- production build проходит без ошибок;
- `dist/index.html` создан;
- assets лежат в `dist/assets`;
- frontend не хранит секреты;
- frontend обращается к backend через `/api` и `/ws`, а не через захардкоженный localhost.

В Docker production-like схеме frontend собирается внутри `frontend/Dockerfile`, а результат отдает Nginx.

## 6. Reverse Proxy

На production домене reverse proxy должен маршрутизировать:

```text
/      -> frontend static files
/api   -> backend
/ws    -> backend WebSocket/STOMP
```

В текущем `docker-compose.yml` эту роль выполняет сервис `frontend`.
Он собирает React-приложение и запускает Nginx на порту:

```env
FRONTEND_PORT=80
```

Проверить:

- `/` открывает React-приложение;
- `/api/...` проксируется в Spring Boot;
- `/ws` поддерживает WebSocket upgrade;
- HTTPS включен;
- HTTP редиректится на HTTPS.

Для WebSocket proxy важно передавать headers:

```text
Upgrade
Connection
Host
X-Forwarded-For
X-Forwarded-Proto
```

## 7. CORS И Origins

Если frontend и backend живут на одном домене через reverse proxy, CORS почти не нужен.

Если frontend и backend на разных доменах, нужно явно разрешить frontend origin:

```env
APP_CORS_ALLOWED_ORIGINS=https://your-domain.com
```

Проверить:

- login из браузера не падает с CORS error;
- приватные REST-запросы проходят с `Authorization`;
- WebSocket подключается к `/ws`;
- STOMP subscription получает сообщения.

## 8. Порты И Firewall

Обычно наружу открываем только:

```text
80
443
```

Внутренние порты:

```text
backend: 8080
postgres: 5432
```

Проверить:

- PostgreSQL не открыт наружу;
- backend не обязательно открыт наружу напрямую, если есть reverse proxy;
- firewall пропускает HTTP/HTTPS;
- домен указывает на сервер.

## 9. Health И Smoke Проверки

После деплоя проверить:

```bash
curl -i https://your-domain.com/
curl -i https://your-domain.com/api
curl -i https://your-domain.com/api-docs
```

Проверить основной сценарий:

1. Открыть frontend.
2. Войти организатором.
3. Создать quiz.
4. Добавить question.
5. Запустить session.
6. Открыть participant room в другом браузере.
7. Подключиться по room code.
8. Показать вопрос.
9. Отправить ответ.
10. Проверить leaderboard.
11. Завершить session.

## 10. Логи

Проверить, что доступны логи:

- backend startup;
- Flyway migrations;
- ошибки подключения к PostgreSQL;
- ошибки JWT/RSA;
- ошибки REST API;
- WebSocket/STOMP connection events.

Минимально:

```bash
docker compose logs backend
docker compose logs postgres
```

## 11. Секреты

Нельзя коммитить:

- реальные PostgreSQL пароли;
- RSA private key;
- production `.env`;
- access tokens;
- любые server credentials.

Можно коммитить:

- `.env.example`;
- public documentation;
- development-only examples без реальных секретов.

## 12. Перед Релизом

Перед деплоем должно быть зелено:

```bash
cd backend
./gradlew test
```

```bash
cd frontend
npm ci
npm run build
```

И должна разворачиваться Docker Compose конфигурация:

```bash
docker compose --env-file .env config
```

Также проверить:

- `git status` не содержит случайных файлов;
- `node_modules` и `dist` не попадают в git;
- `backend/build` не попадает в git;
- production secrets не попадают в git.
