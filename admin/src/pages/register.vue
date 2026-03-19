<script setup>
const router = useRouter()

definePage({
  meta: {
    layout: 'blank',
    public: true,
  },
})

onMounted(() => {
  router.replace({ name: 'login' })
})
</script>

<template>
  <div class="d-flex align-center justify-center h-100">
    <VProgressCircular indeterminate />
  </div>
</template>
