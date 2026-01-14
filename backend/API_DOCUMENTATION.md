# API Документация - Добавление товара

## Описание

После авторизации пользователь автоматически перенаправляется на страницу добавления товара.

## Аутентификация

### Учетные данные для тестирования:
- **Username**: `admin`
- **Password**: `admin`

### Endpoint для входа:
- **URL**: `POST /api/auth/login`
- **Content-Type**: `application/x-www-form-urlencoded`
- **Параметры**: `username=admin&password=admin`

После успешной авторизации происходит автоматический редирект на `/api/admin/products`.

## Добавление товара

### Endpoint
```
POST /api/admin/products
```

### Headers
```
Content-Type: application/json
Cookie: JSESSIONID=<session_id> (получается после авторизации)
```

### Request Body
```json
{
  "name": "Название товара",
  "description": "Описание товара",
  "price": 999.99,
  "oldPrice": 1299.99,
  "sku": "PRODUCT-001",
  "stockQuantity": 100,
  "isActive": true,
  "isFeatured": false,
  "imageUrl": "https://example.com/image.jpg"
}
```

### Обязательные поля:
- `name` - Название товара (3-255 символов)
- `price` - Цена (больше 0)

### Опциональные поля:
- `description` - Описание товара (до 5000 символов)
- `oldPrice` - Старая цена
- `sku` - Артикул товара (уникальный)
- `stockQuantity` - Количество на складе (по умолчанию 0)
- `isActive` - Активен ли товар (по умолчанию true)
- `isFeatured` - Рекомендуемый товар (по умолчанию false)
- `imageUrl` - URL изображения (до 500 символов)

### Response (201 Created)
```json
{
  "id": 1,
  "name": "Название товара",
  "description": "Описание товара",
  "price": 999.99,
  "oldPrice": 1299.99,
  "sku": "PRODUCT-001",
  "stockQuantity": 100,
  "isActive": true,
  "isFeatured": false,
  "imageUrl": "https://example.com/image.jpg",
  "createdAt": "2026-01-09T19:50:00",
  "updatedAt": "2026-01-09T19:50:00"
}
```

### Пример ошибки валидации (400 Bad Request)
```json
{
  "timestamp": "2026-01-09T19:50:00",
  "status": 400,
  "errors": {
    "name": "Название товара обязательно",
    "price": "Цена обязательна"
  },
  "message": "Ошибка валидации данных"
}
```

## Пример использования с curl

### 1. Авторизация
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -d "username=admin&password=admin" \
  -c cookies.txt \
  -v
```

### 2. Добавление товара
```bash
curl -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "name": "Тестовый товар",
    "description": "Описание тестового товара",
    "price": 1999.99,
    "sku": "TEST-001",
    "stockQuantity": 50,
    "isActive": true
  }'
```

## База данных

Таблица `products` создается автоматически при запуске приложения через Liquibase migration:
- Файл миграции: `src/main/resources/db/changelog/changes/001-create-products-table.xml`
- Таблица создается в базе данных `ihome24` на порту `5434`

## Структура таблицы

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    old_price DECIMAL(10,2),
    sku VARCHAR(100),
    stock_quantity INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
