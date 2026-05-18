# iHome24 — Nuxt 3 SSR (витрина)

Публичный сайт с **Server-Side Rendering** для SEO: роботы получают HTML с товарами, ценами и Schema.org без выполнения JS.

## Разработка

```bash
cd frontend-nuxt
cp .env.example .env
npm install
npm run dev
```

- Сайт: http://localhost:3000  
- API: прокси `/api` → `NUXT_API_BASE_SERVER` (по умолчанию http://localhost:8080)

## Продакшен

Собирается через `Dockerfile.prod`, контейнер слушает порт **3000** (Node SSR).

Переменные:

| Переменная | Назначение |
|------------|------------|
| `NUXT_API_BASE_SERVER` | URL Spring API **из контейнера** (`http://backend:8080`) |
| `NUXT_PUBLIC_API_BASE` | URL API для браузера (пусто = тот же origin) |
| `NUXT_PUBLIC_SITE_URL` | Канонический сайт (`https://ihome24.ru`) |

## SSR-маршруты

Серверный рендер: `/`, `/products`, `/products/:id`, `/category/*`, `/support/*`.

Клиент-only (`ssr: false`): `/cart`, `/checkout`, `/login`, `/personal/*`.

## Старый Vite SPA

Каталог `frontend/` — предыдущая версия; в Docker prod используется **frontend-nuxt**.
