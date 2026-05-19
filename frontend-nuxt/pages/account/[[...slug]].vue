<script setup>
/**
 * Личный кабинет: /account, /account/orders, /account/profile, /account/favorites
 * Только CSR (без SSR).
 */
definePageMeta({ layout: 'personal', ssr: false })

const route = useRoute()

const segments = computed(() => {
  const raw = route.params.slug
  if (raw == null || raw === '') return []
  return Array.isArray(raw) ? raw.filter(Boolean) : [String(raw)]
})

const view = computed(() => {
  const first = segments.value[0] || ''
  if (first === '' || first === 'index') return 'index'
  if (first === 'orders') return 'orders'
  if (first === 'profile') return 'profile'
  if (first === 'favorites') return 'favorites'
  return 'unknown'
})

if (view.value === 'unknown') {
  throw createError({ statusCode: 404, statusMessage: 'Страница не найдена' })
}

const titles = {
  index: 'Личный кабинет - iHome24',
  orders: 'Мои заказы - iHome24',
  profile: 'Профиль - iHome24',
  favorites: 'Избранное - iHome24',
}

useSeoMeta({
  title: computed(() => titles[view.value] || titles.index),
  robots: 'noindex',
})
</script>

<template>
  <PagesPersonalIndex v-if="view === 'index'" />
  <PagesPersonalOrders v-else-if="view === 'orders'" />
  <PagesPersonalProfile v-else-if="view === 'profile'" />
  <PagesPersonalPlaceholder v-else-if="view === 'favorites'" />
</template>
