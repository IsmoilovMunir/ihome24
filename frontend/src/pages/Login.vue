<template>
  <div class="min-h-[60vh] flex items-center justify-center py-12">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6 text-center">Вход</h1>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            <div v-if="typeof error === 'object'">
              <div v-for="(messages, field) in error" :key="field" class="mb-1">
                <div v-for="message in messages" :key="message">{{ message }}</div>
              </div>
            </div>
            <div v-else>{{ error }}</div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Email или имя пользователя *
            </label>
            <input
              v-model="form.email"
              type="text"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="email@example.com"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Пароль *
            </label>
            <input
              v-model="form.password"
              type="password"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            :disabled="authStore.loading"
            class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ authStore.loading ? 'Вход...' : 'Войти' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-gray-600">
            Нет аккаунта?
            <router-link to="/register" class="text-primary-600 hover:text-primary-700 font-semibold">
              Зарегистрироваться
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  email: '',
  password: '',
})

const error = ref(null)

const handleLogin = async () => {
  error.value = null
  const result = await authStore.login(form.value)
  
  if (result.success) {
    router.push('/')
  } else {
    error.value = result.error
  }
}
</script>
