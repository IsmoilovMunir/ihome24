<script setup>
/** Legacy /personal/* → /account/* (301) */
const route = useRoute()
const segments = route.params.slug
const suffix = segments
  ? (Array.isArray(segments) ? segments.join('/') : String(segments))
  : ''
const target = suffix ? `/account/${suffix}` : '/account'
await navigateTo({ path: target, query: route.query }, { redirectCode: 301 })
</script>

<template>
  <div />
</template>
