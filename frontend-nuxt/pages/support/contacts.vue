<script setup>
const siteUrl = useSiteUrl()
useSeoMeta({
  title: 'Контакты - iHome24',
  description: 'Контакты iHome24: способы связи и поддержка.',
  robots: 'index, follow',
})
useHead({ link: [{ rel: 'canonical', href: `${siteUrl}/support/contacts` }] })
</script>

<template>
  <PagesContacts />
</template>
