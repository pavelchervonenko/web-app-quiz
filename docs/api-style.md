# Стиль API

Этот документ описывает стиль ответов API для backend-части Web App Quiz.
Цель - сделать API предсказуемым для frontend-части, удобным для тестирования и простым для развития.

## Общие правила

- Контроллеры возвращают DTO, а не JPA-сущности.
- DTO описываем через Java records.
- Request DTO валидируем через Jakarta Bean Validation.
- Бизнес-ошибки выбрасываем из сервисов и обрабатываем глобально.
- Даты и время возвращаем в формате ISO-8601.
- Enum возвращаем строкой, например `FINISHED` или `PARTICIPANT`.
- В приватных endpoint-ах текущего пользователя берем из `Authentication`.
- Пользователь может читать и изменять только свои приватные данные, если endpoint явно не является публичным.

## Успешные ответы

Используем HTTP-статусы последовательно.

| Операция | Статус | Тело ответа |
| --- | --- | --- |
| Получить один ресурс | `200 OK` | DTO ресурса |
| Получить список | `200 OK` | JSON-массив DTO |
| Создать ресурс | `201 Created` | DTO созданного ресурса |
| Обновить ресурс | `200 OK` | DTO обновленного ресурса |
| Удалить ресурс | `204 No Content` | Пустое тело |
| Выполнить команду/действие | `200 OK` или `201 Created` | DTO результата или DTO состояния |

Пример DTO ресурса:

```json
{
  "id": "8a5b6d90-0cf9-4c55-8e34-67d6f39a5721",
  "title": "Spring Boot Quiz",
  "status": "DRAFT"
}
```

Пример списка:

```json
[
  {
    "sessionId": "c063f0eb-fcd7-442d-81ed-488ce35fb15f",
    "quizTitle": "Spring Boot Quiz",
    "roomCode": "AB12CD",
    "status": "FINISHED"
  }
]
```

## Ошибки

Все обработанные ошибки возвращаем в едином формате:

```json
{
  "timestamp": "2026-06-05T12:28:54.699655400Z",
  "status": 409,
  "error": "Conflict",
  "message": "User with this email already exists",
  "path": "/api/auth/register",
  "fieldErrors": {}
}
```

Ошибки валидации кладем в `fieldErrors`:

```json
{
  "timestamp": "2026-06-05T12:28:54.699655400Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/quizzes",
  "fieldErrors": {
    "title": "must not be blank"
  }
}
```

## Статусы ошибок

| Статус | Когда использовать |
| --- | --- |
| `400 Bad Request` | Некорректные данные запроса или недопустимое действие в текущем состоянии |
| `401 Unauthorized` | Пользователь не авторизован |
| `403 Forbidden` | Пользователь авторизован, но у него нет доступа |
| `404 Not Found` | Ресурс не существует или недоступен текущему пользователю |
| `409 Conflict` | Дубликат данных или конфликт бизнес-правил |
| `500 Internal Server Error` | Неожиданная ошибка сервера |

## Именование endpoint-ов

- Используем существительные во множественном числе: `/api/quizzes`, `/api/sessions`, `/api/users`.
- Используем вложенные пути, когда ресурс принадлежит другому ресурсу:
  - `POST /api/quizzes/{quizId}/questions`
  - `POST /api/quizzes/{quizId}/sessions`
- Названия действий используем только для настоящих команд:
  - `POST /api/sessions/{sessionId}/questions/next`
  - `POST /api/sessions/{sessionId}/questions/current/close`
  - `POST /api/sessions/{sessionId}/finish`
- Для текущего авторизованного пользователя используем `/me`:
  - `GET /api/users/me`
  - `PATCH /api/users/me`

## Списки и пагинация

Для MVP endpoint-ы со списками могут возвращать обычный JSON-массив.

Пример:

```json
[
  {
    "id": "8a5b6d90-0cf9-4c55-8e34-67d6f39a5721",
    "title": "Spring Boot Quiz"
  }
]
```

Когда понадобится пагинация, вводим отдельный DTO для пагинированного ответа.
Нельзя молча менять контракт уже существующего endpoint-а.

Рекомендуемая будущая форма:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalItems": 120,
  "totalPages": 6
}
```

## Именование DTO

- `SomethingCreateRequest` - request для создания.
- `SomethingUpdateRequest` - request для обновления.
- `SomethingDTO` - подробное представление ресурса.
- `SomethingSummaryDTO` - короткое представление для списков.
- `SomethingResponse` - ответ, который не является обычным представлением ресурса, например auth или join response.

Примеры:

- `QuizCreateRequest`
- `QuizUpdateRequest`
- `QuizDetailsDTO`
- `QuizSummaryDTO`
- `AuthResponse`
- `JoinQuizSessionResponse`

## Правила безопасности

- Публичные endpoint-ы явно настраиваем в security config.
- Приватные endpoint-ы должны работать через текущего авторизованного пользователя.
- Проверки ролей можно добавлять на уровне endpoint-а через method security.
- Проверки владения ресурсом должны быть в сервисах, а не в контроллерах.

Пример:

```java
@PreAuthorize("hasRole('ORGANIZER')")
```

## Правила тестирования

- Controller-тесты проверяют HTTP-статусы и DTO ответов.
- Тесты должны покрывать успешные сценарии, авторизацию, доступы и важные бизнес-ошибки.
- Тесты должны проверять, что пользователь не может видеть или изменять чужие приватные данные.
- Интеграционные тесты пишем через `MockMvc`, реальные репозитории и `test` profile.
