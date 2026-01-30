<template>
  <div class="min-h-screen flex flex-col bg-[#3A3331]">
    <Header />
    <main class="flex-grow pt-[60px] md:pt-[110px] pb-[70px] md:pb-0">
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
