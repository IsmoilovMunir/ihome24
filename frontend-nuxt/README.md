# iHome24 — Nuxt 3 SSR (витрина)

Публичный сайт с **Server-Side Rendering** для SEO.

> Задача 1.1: проект инициализирован в каталоге `frontend-nuxt` (аналог `ihome24-nuxt` из ТЗ).

## Структура

```
frontend-nuxt/
├── pages/          # маршруты (file-based routing)
├── components/     # Vue-компоненты
├── composables/    # переиспользуемая логика (useApiBase, useProductSeo, …)
├── layouts/        # default, personal
├── stores/         # Pinia
├── plugins/        # api.ts
├── assets/         # стили
└── public/         # статика
```

## Разработка

```bash
cd frontend-nuxt
cp .env.example .env
npm install
npm run dev
```

- Сайт: http://localhost:3000
- API: прокси `/api` → `NUXT_API_BASE_SERVER` (по умолчанию http://localhost:8080)

## Модули (Sprint 1.1)

| Модуль | Назначение |
|--------|------------|
| `@nuxtjs/sitemap` | `/sitemap.xml` — URL из Spring API (`/api/__sitemap__/urls`) |
| `@nuxtjs/robots` | robots.txt и meta robots |
| `nuxt-schema-org` | JSON-LD Organization / Product |
| `@pinia/nuxt` | состояние |
| `@nuxtjs/tailwindcss` | стили |

В **production** `robots.txt` — Spring; **`/sitemap.xml`** — Nuxt (`@nuxtjs/sitemap`, данные из Spring API).

## API (`useApi`)

```ts
const { api, get, post } = useApi()

// SSR → http://localhost:8080/api/…
// CSR → /api/… (относительный путь через proxy)
const products = await api('/products')
```

Глобально: `$apiFetch` (плагин `api-fetch.ts`). Ошибки **404** и **5xx** → `createError` и страница `error.vue`.

Axios-сервисы (`$productApi`, …) — плагин `api.ts` (корзина, auth, checkout).

## Переменные окружения

| Переменная | `runtimeConfig` | Назначение |
|------------|-----------------|------------|
| `NUXT_PUBLIC_API_BASE` | `public.apiBase` | URL API в браузере (пусто = `/api` на том же origin) |
| `NUXT_API_BASE_SERVER` | `apiBaseServer` | URL Spring для SSR и devProxy |
| `NUXT_PUBLIC_SITE_URL` | `public.siteUrl` | Канонический сайт (`https://ihome24.ru`) |

## Продакшен

Собирается через `Dockerfile.prod`, контейнер слушает порт **3000** (Node SSR).

## SSR-маршруты

Серверный рендер: `/`, `/products`, `/products/:id`, `/category/*`, `/support/*`.

Клиент-only (`ssr: false`): `/cart`, `/checkout`, `/login`, `/personal/*`.

## Старый Vite SPA

Каталог `frontend/` — предыдущая версия; в Docker prod используется **frontend-nuxt**.
