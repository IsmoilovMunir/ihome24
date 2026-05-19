<script setup>
/**
 * Catch-all для несуществующих URL (/f, /random-path, …).
 * Без throw createError — иначе в dev показывается Nuxt error overlay.
 */
definePageMeta({ layout: false })

setResponseStatus(404, 'Страница не найдена')

useSeoMeta({
  title: '404 — Страница не найдена | iHome24',
  description: 'Запрашиваемая страница или товар не существует.',
  robots: 'noindex, nofollow',
})
</script>

<template>
  <PagesNotFound />
</template>
