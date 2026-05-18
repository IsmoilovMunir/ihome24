<template>
  <div
    class="min-h-screen flex flex-col"
    :class="isCheckoutFlow ? 'bg-[#2E2826]' : 'bg-[#3A3331]'"
  >
    <Header />
    <main
      class="flex-grow"
      :class="isCheckoutFlow ? 'pt-[60px] md:pt-[60px] pb-24 md:pb-0' : 'pt-[60px] md:pt-[110px] pb-24 md:pb-0'"
    >
      <slot />
    </main>
    <Footer />
    <MobileBottomMenu />
  </div>
</template>

<script setup>
const route = useRoute()
const isCheckoutFlow = computed(() => route.path === '/cart' || route.path === '/checkout')
</script>
