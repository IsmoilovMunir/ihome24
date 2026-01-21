# Быстрый старт - Регистрация администратора

## Проблема: Ошибка "Invalid email or password" при входе

Это происходит потому, что в базе данных еще нет пользователей. Нужно сначала зарегистрировать администратора.

## Решение 1: Перезапустить приложение и использовать API (РЕКОМЕНДУЕТСЯ)

### Шаг 1: Перезапустите бэкенд
Остановите текущий процесс и запустите заново:
```bash
cd /Users/munirismoilov/ihome24/backend
mvn spring-boot:run
```

### Шаг 2: Зарегистрируйте администратора
После перезапуска выполните:
```bash
curl -X POST http://localhost:8080/api/publicapi/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin",
    "email": "admin@demo.com",
    "fullName": "Администратор",
    "roleName": "admin"
  }'
```

### Шаг 3: Войдите в систему
Теперь вы можете войти с:
- Email: `admin@demo.com`
- Password: `admin`

## Решение 2: Через H2 консоль (если API не работает)

### Шаг 1: Откройте H2 консоль
1. Откройте в браузере: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:ihome24`
3. Username: `sa`
4. Password: (оставьте пустым)

### Шаг 2: Выполните SQL скрипт
Скопируйте и выполните SQL из файла `register-admin.sql`:

```sql
-- Найти ID роли admin
SELECT id, name FROM roles WHERE name = 'admin';

-- Создать администратора (замените :role_id на ID из предыдущего запроса)
INSERT INTO users (
    username, password, email, full_name, role_id, status, 
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    current_plan, billing, created_at, updated_at
) VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- пароль: admin
    'admin@demo.com',
    'Администратор',
    1, -- замените на реальный ID роли admin
    'ACTIVE',
    true, true, true, true,
    'enterprise',
    'Автоматическое списание',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

### Шаг 3: Проверьте создание
```sql
SELECT u.id, u.username, u.email, r.name as role 
FROM users u 
LEFT JOIN roles r ON u.role_id = r.id;
```

## Решение 3: Использовать скрипт (после перезапуска)

```bash
cd /Users/munirismoilov/ihome24/backend
./register-admin.sh
```

## Важно!

**После изменения SecurityConfig необходимо перезапустить приложение**, чтобы изменения вступили в силу.

Если вы не можете перезапустить приложение прямо сейчас, используйте Решение 2 (H2 консоль).
