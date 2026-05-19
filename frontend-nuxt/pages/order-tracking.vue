<script setup>
const siteUrl = useSiteUrl()
useSeoMeta({
  title: 'Отслеживание заказа - iHome24',
  description: 'Проверка статуса заказа iHome24.',
  robots: 'index, follow',
})
useHead({ link: [{ rel: 'canonical', href: `${siteUrl}/order-tracking`, key: 'canonical' }] })
</script>

<template>
  <PagesOrderTracking />
</template>
