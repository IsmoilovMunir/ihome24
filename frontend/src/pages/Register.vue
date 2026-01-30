<template>
  <div class="min-h-[60vh] flex items-center justify-center py-12">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6 text-center">Регистрация</h1>

        <form @submit.prevent="handleRegister" class="space-y-4">
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
              Имя пользователя *
            </label>
            <input
              v-model="form.username"
              type="text"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="username"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Email *
            </label>
            <input
              v-model="form.email"
              type="email"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="email@example.com"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              ФИО *
            </label>
            <input
              v-model="form.fullName"
              type="text"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="Иванов Иван Иванович"
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
              minlength="6"
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="••••••••"
            />
            <p class="text-xs text-gray-500 mt-1">Минимум 6 символов</p>
          </div>

          <button
            type="submit"
            :disabled="authStore.loading"
            class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ authStore.loading ? 'Регистрация...' : 'Зарегистрироваться' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-gray-600">
            Уже есть аккаунт?
            <router-link to="/login" class="text-primary-600 hover:text-primary-700 font-semibold">
              Войти
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
  username: '',
  email: '',
  fullName: '',
  password: '',
})

const error = ref(null)

const handleRegister = async () => {
  error.value = null
  
  if (form.value.password.length < 6) {
    error.value = { password: ['Пароль должен содержать минимум 6 символов'] }
    return
  }

  const result = await authStore.register(form.value)
  
  if (result.success) {
    alert('Регистрация успешна! Теперь вы можете войти.')
    router.push('/login')
  } else {
    error.value = result.error
  }
}
</script>
