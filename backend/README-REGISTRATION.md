# Регистрация администратора и менеджера

## Описание

Администратор и менеджер теперь регистрируются через API бэкенда, а не создаются автоматически при запуске приложения.

## Способы регистрации

### 1. Через скрипты (рекомендуется)

**Регистрация администратора:**
```bash
cd backend
./register-admin.sh
```

Или с кастомными данными:
```bash
./register-admin.sh admin admin@example.com mypassword "Имя Администратора"
```

**Регистрация менеджера:**
```bash
./register-manager.sh
```

Или с кастомными данными:
```bash
./register-manager.sh manager manager@example.com mypassword "Имя Менеджера"
```

### 2. Через curl

**Регистрация администратора:**
```bash
curl -X POST http://localhost:8080/api/publicapi/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin",
    "email": "admin@demo.com",
    "fullName": "Администратор",
    "roleName": "admin",
    "currentPlan": "enterprise",
    "billing": "Автоматическое списание"
  }'
```

**Регистрация менеджера:**
```bash
curl -X POST http://localhost:8080/api/publicapi/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager",
    "password": "manager",
    "email": "manager@demo.com",
    "fullName": "Менеджер",
    "roleName": "manager",
    "currentPlan": "company",
    "billing": "Автоматическое списание"
  }'
```

### 3. Через фронтенд

После запуска бэкенда и фронтенда, можно использовать форму регистрации в админке или создать пользователей через API `/api/apps/users` (POST запрос).

## Endpoint для регистрации администратора/менеджера

**POST** `/api/publicapi/auth/register-admin`

**Тело запроса:**
```json
{
  "username": "admin",
  "password": "admin",
  "email": "admin@demo.com",
  "fullName": "Администратор",
  "roleName": "admin",  // или "manager"
  "currentPlan": "enterprise",
  "billing": "Автоматическое списание",
  "company": "Компания",
  "country": "Россия",
  "contact": "+7-999-123-45-67"
}
```

**Ответ (201 Created):**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@demo.com",
  "fullName": "Администратор",
  "role": "admin",
  "status": "ACTIVE",
  ...
}
```

## Важные замечания

1. **Первый запуск:** После первого запуска бэкенда необходимо зарегистрировать хотя бы одного администратора для доступа в админку.

2. **Роли:** Endpoint `/register-admin` принимает только роли `admin` и `manager`. Для других ролей используйте обычную регистрацию `/register`.

3. **Безопасность:** В продакшене рекомендуется ограничить доступ к endpoint `/register-admin` или добавить дополнительную проверку (например, секретный ключ).

4. **Обычная регистрация:** Обычные пользователи могут регистрироваться через `/api/publicapi/auth/register` и получат роль `users` по умолчанию.
