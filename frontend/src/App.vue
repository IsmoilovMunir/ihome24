<template>
  <div class="min-h-screen flex flex-col bg-[#3A3331]">
    <Header />
    <main class="flex-grow pt-[110px]">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<script setup>
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
</script>
