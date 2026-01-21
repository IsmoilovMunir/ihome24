# Пользовательская часть интернет-магазина (Vue.js)

Это frontend-приложение для клиентов интернет-магазина, построенное на Vue 3.

## Архитектура

```
Пользовательский сайт (Vue)
            |
            |  HTTP / JSON (REST API)
            |
        Backend (Spring Boot)
            |
     ------------------------
     |                      |
Админка (Vue)         База данных
```

## Технологии

- **Vue 3** - фреймворк для создания пользовательского интерфейса
- **Vue Router** - маршрутизация
- **Pinia** - управление состоянием
- **Axios** - HTTP-клиент для работы с API
- **Vite** - сборщик и dev-сервер

## Структура проекта

```
frontend/
├── src/
│   ├── components/      # Переиспользуемые компоненты
│   │   ├── Header.vue
│   │   ├── Footer.vue
│   │   └── ProductCard.vue
│   ├── pages/          # Страницы приложения
│   │   ├── Home.vue
│   │   ├── Products.vue
│   │   ├── ProductDetail.vue
│   │   ├── Cart.vue
│   │   └── Checkout.vue
│   ├── router/         # Конфигурация маршрутов
│   │   └── index.js
│   ├── store/          # Pinia stores
│   │   ├── cart.js
│   │   └── products.js
│   ├── services/       # API сервисы
│   │   └── api.js
│   ├── App.vue         # Корневой компонент
│   ├── main.js         # Точка входа
│   └── style.css       # Глобальные стили
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## Установка и запуск

1. Установите зависимости:
```bash
npm install
```

2. Запустите dev-сервер:
```bash
npm run dev
```

Приложение будет доступно по адресу `http://localhost:3000`

3. Сборка для production:
```bash
npm run build
```

## API Endpoints

Приложение ожидает следующие endpoints от backend:

### Товары
- `GET /api/products` - получить список товаров
- `GET /api/products/{id}` - получить товар по ID
- `GET /api/products/search?q={query}` - поиск товаров

### Заказы
- `POST /api/orders` - создать заказ

## Конфигурация

В `vite.config.js` настроен proxy для API запросов:
- Все запросы к `/api` проксируются на `http://localhost:8080` (backend)

Если ваш backend работает на другом порту, измените настройки в `vite.config.js`.

## Основные функции

- ✅ Просмотр каталога товаров
- ✅ Детальная страница товара
- ✅ Поиск товаров
- ✅ Корзина покупок
- ✅ Оформление заказа
- ✅ Адаптивный дизайн

## Взаимодействие с Backend

Приложение общается **только** с backend через REST API. Никакого прямого взаимодействия с админкой нет.

Все данные (товары, заказы) получаются и отправляются через backend API.
