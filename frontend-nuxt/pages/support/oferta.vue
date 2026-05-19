<script setup>
const siteUrl = useSiteUrl()
useSeoMeta({
  title: 'Оферта - iHome24',
  description: 'Публичная оферта интернет-магазина iHome24.',
  robots: 'index, follow',
})
useHead({ link: [{ rel: 'canonical', href: `${siteUrl}/support/oferta`, key: 'canonical' }] })
</script>

<template>
  <PagesOferta />
</template>
