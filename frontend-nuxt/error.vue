<script setup>
const error = useError()

const title = computed(() => {
  if (error.value?.statusCode === 404) return 'Страница не найдена'
  return 'Ошибка'
})

function goHome() {
  clearError({ redirect: '/' })
}
</script>

<template>
  <div class="min-h-screen bg-[#3A3331] flex flex-col items-center justify-center px-4 text-white">
    <p class="text-6xl font-bold mb-2">{{ error?.statusCode || 500 }}</p>
    <h1 class="text-xl mb-6">{{ title }}</h1>
    <NuxtLink
      to="/"
      class="px-6 py-3 rounded-full bg-white text-[#3A3331] font-medium"
      @click.prevent="goHome"
    >
      На главную
    </NuxtLink>
  </div>
</template>
