# Деплой На VPS

Короткая инструкция для первого запуска проекта на арендованном VPS.

## 1. Подготовить Сервер

На сервере нужны:

- Git;
- Docker;
- Docker Compose plugin.

Проверка:

```bash
git --version
docker --version
docker compose version
```

## 2. Склонировать Проект

```bash
git clone https://github.com/pavelchervonenko/web-app-quiz.git
cd web-app-quiz
```

## 3. Создать `.env`

```bash
cp .env.example .env
nano .env
```

Минимально поменять:

```env
POSTGRES_PASSWORD=strong-password
SPRING_DATASOURCE_PASSWORD=strong-password
APP_CORS_ALLOWED_ORIGIN_PATTERNS=http://your-server-ip
FRONTEND_PORT=80
```

Если уже есть домен:

```env
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://your-domain.com
```

## 4. Сгенерировать RSA Ключи

```bash
mkdir -p deploy/secrets

openssl genpkey \
  -algorithm RSA \
  -out deploy/secrets/jwt-private.pem \
  -pkeyopt rsa_keygen_bits:2048

openssl rsa \
  -in deploy/secrets/jwt-private.pem \
  -pubout \
  -out deploy/secrets/jwt-public.pem

chmod 600 deploy/secrets/jwt-private.pem
chmod 644 deploy/secrets/jwt-public.pem
```

Пути в `.env` должны совпадать:

```env
JWT_PUBLIC_KEY_PATH=./deploy/secrets/jwt-public.pem
JWT_PRIVATE_KEY_PATH=./deploy/secrets/jwt-private.pem
RSA_PUBLIC_KEY=file:/run/secrets/jwt-public.pem
RSA_PRIVATE_KEY=file:/run/secrets/jwt-private.pem
```

## 5. Проверить Compose Конфигурацию

```bash
docker compose --env-file .env config
```

Если переменные заполнены неправильно, ошибка должна появиться на этом шаге.

## 6. Запустить Приложение

```bash
docker compose --env-file .env up -d --build
```

Проверить контейнеры:

```bash
docker compose ps
```

Посмотреть логи:

```bash
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
```

## 7. Проверить В Браузере

Открыть:

```text
http://your-server-ip/
```

Проверить:

- открывается frontend;
- работает login/register;
- можно создать quiz;
- можно запустить session;
- participant room получает обновления.

API docs:

```text
http://your-server-ip/api-docs
http://your-server-ip/swagger-ui.html
```

## 8. Обновить Проект На Сервере

```bash
git pull
docker compose --env-file .env up -d --build
```

Если нужно посмотреть новые логи:

```bash
docker compose logs -f backend
```

## 9. Остановить

Остановить контейнеры без удаления данных PostgreSQL:

```bash
docker compose down
```

Остановить и удалить volume PostgreSQL:

```bash
docker compose down -v
```

`down -v` удалит данные БД, поэтому использовать осторожно.

## 10. HTTPS И Домен

Первый запуск можно проверить по IP и HTTP.

Для production лучше добавить:

- домен;
- HTTPS;
- reverse proxy на host-уровне или донастроить текущий Nginx;
- redirect HTTP -> HTTPS.

Этот шаг стоит делать после того, как приложение стабильно запускается через Docker Compose.
