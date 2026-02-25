<template>
  <div class="min-h-[60vh] flex items-center justify-center py-12">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6 text-center">
          {{ step === 'phone' ? 'Вход' : step === 'code' ? 'Введите код' : 'Завершите регистрацию' }}
        </h1>

        <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          <div v-if="typeof error === 'object'">
            <div v-for="(messages, field) in error" :key="field" class="mb-1">
              <div v-for="message in messages" :key="message">{{ message }}</div>
            </div>
          </div>
          <div v-else>{{ error }}</div>
        </div>

        <!-- Шаг 1: Номер телефона -->
        <form v-if="step === 'phone'" @submit.prevent="handleSendCode" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Номер телефона *</label>
            <input
              v-model="form.phone"
              type="tel"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="+7 (999) 123-45-67"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Отправка...' : 'Получить код' }}
          </button>
        </form>

        <!-- Шаг 2: Код из SMS -->
        <form v-else-if="step === 'code'" @submit.prevent="handleVerifyCode" class="space-y-4">
          <p class="text-sm text-gray-600 mb-2">
            Код отправлен на {{ form.phone }}
          </p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Код из SMS *</label>
            <input
              v-model="form.code"
              type="text"
              inputmode="numeric"
              maxlength="6"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent text-center text-lg tracking-widest"
              placeholder="123456"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Проверка...' : 'Войти' }}
          </button>
          <button
            type="button"
            @click="handleSendCode"
            :disabled="loading || resendCooldown > 0"
            class="w-full text-sm text-primary-600 hover:text-primary-700 disabled:text-gray-400"
          >
            {{ resendCooldown > 0 ? `Отправить повторно через ${resendCooldown} сек` : 'Отправить код повторно' }}
          </button>
        </form>

        <!-- Шаг 3: Регистрация (ФИО, email) -->
        <form v-else-if="step === 'registration'" @submit.prevent="handleCompleteRegistration" class="space-y-4">
          <p class="text-sm text-gray-600 mb-2">
            Вы впервые с нами. Укажите данные для завершения регистрации.
          </p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">ФИО *</label>
            <input
              v-model="form.fullName"
              type="text"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="Иванов Иван Иванович"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Email *</label>
            <input
              v-model="form.email"
              type="email"
              required
              class="w-full px-4 py-2 border rounded-md focus:ring-2 focus:ring-primary-600 focus:border-transparent"
              placeholder="email@example.com"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Регистрация...' : 'Зарегистрироваться' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-gray-600" v-if="step !== 'phone'">
            <button type="button" @click="resetFlow" class="text-primary-600 hover:text-primary-700 font-semibold">
              Назад
            </button>
          </p>
          <p class="text-gray-600 mt-2" v-if="step === 'phone'">
            <router-link to="/register" class="text-primary-600 hover:text-primary-700 font-semibold">
              Регистрация
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const step = ref('phone')
const loading = ref(false)
const error = ref(null)
const resendCooldown = ref(0)
let cooldownInterval = null

const form = ref({
  phone: '',
  code: '',
  fullName: '',
  email: '',
  registrationToken: '',
})

const handleSendCode = async () => {
  error.value = null
  loading.value = true
  try {
    const res = await authApi.sendSmsCode(form.value.phone)
    if (res.data.success) {
      step.value = 'code'
      form.value.code = ''
      startResendCooldown()
    } else if (res.data.errors) {
      error.value = res.data.errors
    }
  } catch (e) {
    error.value = e.response?.data?.errors || { phone: ['Не удалось отправить код'] }
  } finally {
    loading.value = false
  }
}

const handleVerifyCode = async () => {
  error.value = null
  loading.value = true
  try {
    const res = await authApi.verifySmsCode(form.value.phone, form.value.code)
    const data = res.data
    if (data.needsRegistration && data.registrationToken) {
      form.value.registrationToken = data.registrationToken
      step.value = 'registration'
      form.value.fullName = ''
      form.value.email = ''
    } else if (data.accessToken && data.userData) {
      authStore.setAuth(data.accessToken, data.userData)
      router.push('/')
    } else {
      error.value = { code: ['Неверный ответ сервера'] }
    }
  } catch (e) {
    error.value = e.response?.data?.errors || { code: ['Неверный или истёкший код'] }
  } finally {
    loading.value = false
  }
}

const handleCompleteRegistration = async () => {
  error.value = null
  loading.value = true
  try {
    const res = await authApi.completeRegistration({
      registrationToken: form.value.registrationToken,
      fullName: form.value.fullName,
      email: form.value.email,
    })
    const data = res.data
    if (data.accessToken && data.userData) {
      authStore.setAuth(data.accessToken, data.userData)
      router.push('/')
    } else {
      error.value = { fullName: ['Ошибка регистрации'] }
    }
  } catch (e) {
    error.value = e.response?.data?.errors || { fullName: ['Ошибка регистрации'] }
  } finally {
    loading.value = false
  }
}

const startResendCooldown = () => {
  resendCooldown.value = 120 // 2 минуты, как на бэкенде
  if (cooldownInterval) clearInterval(cooldownInterval)
  cooldownInterval = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0) clearInterval(cooldownInterval)
  }, 1000)
}

const resetFlow = () => {
  step.value = 'phone'
  form.value.code = ''
  form.value.registrationToken = ''
  form.value.fullName = ''
  form.value.email = ''
  error.value = null
}

onMounted(() => {
  return () => {
    if (cooldownInterval) clearInterval(cooldownInterval)
  }
})
</script>
