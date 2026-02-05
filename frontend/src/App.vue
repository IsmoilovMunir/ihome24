<template>
  <div class="min-h-screen flex flex-col" :class="$route.path === '/cart' || $route.path === '/checkout' ? 'bg-[#2E2826]' : 'bg-[#3A3331]'">
    <Header />
    <main class="flex-grow" :class="$route.path === '/cart' ? 'pt-[60px] md:pt-[60px] pb-24 md:pb-0' : 'pt-[60px] md:pt-[110px] pb-24 md:pb-0'">
      <router-view />
    </main>
    <Footer />
    <MobileBottomMenu />
  </div>
</template>

<script setup>
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
import MobileBottomMenu from './components/MobileBottomMenu.vue'
</script>
