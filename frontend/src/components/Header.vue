<template>
  <header class="header-component bg-[#302423]/70 backdrop-blur-xl shadow-2xl sticky top-0 z-50 border-b border-white/20 backdrop-saturate-150">
    <div class="container mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link to="/" class="flex items-center space-x-2">
          <span class="text-2xl font-bold text-white">iHome24</span>
        </router-link>

        <!-- Navigation -->
        <nav class="hidden md:flex items-center space-x-6">
          <router-link
            to="/"
            class="text-gray-200 hover:text-primary-400 transition-colors"
            active-class="text-primary-400 font-semibold"
          >
            Главная
          </router-link>
          <router-link
            to="/products"
            class="text-gray-200 hover:text-primary-400 transition-colors"
            active-class="text-primary-400 font-semibold"
          >
            Каталог
          </router-link>
        </nav>

        <!-- Right side -->
        <div class="flex items-center space-x-4">
          <!-- Cart -->
          <router-link
            to="/cart"
            class="relative p-2 text-gray-200 hover:text-primary-400 transition-colors"
          >
            <svg
              class="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
              />
            </svg>
            <span
              v-if="cartStore.totalItems > 0"
              class="absolute top-0 right-0 bg-primary-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center"
            >
              {{ cartStore.totalItems }}
            </span>
          </router-link>

          <!-- Auth -->
          <div v-if="authStore.isAuthenticated" class="flex items-center space-x-2">
            <span class="text-sm text-gray-200">{{ authStore.user?.fullName || authStore.user?.username }}</span>
            <button
              @click="handleLogout"
              class="px-4 py-2 text-sm text-gray-200 hover:text-primary-400 transition-colors"
            >
              Выход
            </button>
          </div>
          <div v-else class="flex items-center space-x-2">
            <router-link
              to="/login"
              class="px-4 py-2 text-sm text-gray-200 hover:text-primary-400 transition-colors"
            >
              Вход
            </router-link>
            <router-link
              to="/register"
              class="px-4 py-2 text-sm bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors"
            >
              Регистрация
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const cartStore = useCartStore()
const authStore = useAuthStore()
const router = useRouter()

const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.header-component {
  font-family: "helvetica", sans-serif;
}
</style>
