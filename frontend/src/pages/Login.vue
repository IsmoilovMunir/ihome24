<template>
  <div class="min-h-[60vh] flex items-center justify-center py-12">
    <div class="w-full max-w-md">
      <div class="rounded-2xl border border-white/10 bg-[#2E2826] p-8 shadow-lg shadow-black/40">
        <h1
          class="text-3xl md:text-[32px] font-semibold mb-6 text-center tracking-[0.08em] uppercase text-white"
          style="font-family: 'akzidenz-extended', system-ui, -apple-system, BlinkMacSystemFont, sans-serif"
        >
          {{ step === 'phone' ? 'Вход' : step === 'code' ? 'Введите код' : 'Завершите регистрацию' }}
        </h1>

        <div v-if="error" class="bg-red-500/10 border border-red-400/60 text-red-100 px-4 py-3 rounded-lg mb-4 text-sm">
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
            <label class="block text-sm font-medium text-gray-200 mb-1">Номер телефона *</label>
            <input
              v-model="form.phone"
              type="tel"
              required
              class="w-full px-4 py-2 rounded-md border border-white/15 bg-white/5 text-white placeholder:text-gray-500 focus:ring-2 focus:ring-primary-600 focus:border-transparent outline-none"
              placeholder="+7 (999) 123-45-67"
            />
          </div>
          <div class="flex items-start gap-3 text-xs text-gray-300">
            <label class="flex items-start gap-2 cursor-pointer select-none">
              <input
                v-model="consentChecked"
                type="checkbox"
                class="mt-1 h-4 w-4 rounded border border-white/30 bg-white/5 text-[#F47427] focus:ring-1 focus:ring-[#F47427]"
              />
              <span>
                Я подтверждаю своё
                <button
                  type="button"
                  class="text-[#F47427] hover:text-[#ff8c42] underline underline-offset-2"
                  @click.stop.prevent="showPolicyModal = true"
                >
                  согласие на обработку персональных данных
                </button>
                и получение рассылок
              </span>
            </label>
          </div>
          <button
            type="submit"
            :disabled="loading || !consentChecked"
            class="w-full bg-[#F47427] text-white py-3 rounded-md hover:bg-[#ff8c42] disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Отправка...' : 'Войти' }}
          </button>
        </form>

        <!-- Шаг 2: Код из SMS -->
        <form v-else-if="step === 'code'" @submit.prevent="handleVerifyCode" class="space-y-4">
          <p class="text-sm text-gray-300 mb-2">
            Код отправлен на {{ form.phone }}
          </p>
          <div>
            <label class="block text-sm font-medium text-gray-200 mb-1">Код из SMS *</label>
            <input
              v-model="form.code"
              type="text"
              inputmode="numeric"
              maxlength="4"
              required
              class="w-full px-4 py-2 rounded-md border border-white/15 bg-white/5 text-white placeholder:text-gray-500 focus:ring-2 focus:ring-primary-600 focus:border-transparent text-center text-lg tracking-widest outline-none"
              placeholder="1234"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-[#F47427] text-white py-3 rounded-md hover:bg-[#ff8c42] disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Проверка...' : 'Войти' }}
          </button>
          <button
            type="button"
            @click="handleSendCode"
            :disabled="loading || resendCooldown > 0"
            class="w-full text-sm text-primary-300 hover:text-primary-200 disabled:text-gray-500"
          >
            {{ resendCooldown > 0 ? `Отправить повторно через ${resendCooldown} сек` : 'Отправить код повторно' }}
          </button>
        </form>

        <!-- Шаг 3: Регистрация (ФИО, email) -->
        <form v-else-if="step === 'registration'" @submit.prevent="handleCompleteRegistration" class="space-y-4">
          <p class="text-sm text-gray-300 mb-2">
            Вы впервые с нами. Укажите данные для завершения регистрации.
          </p>
          <div>
            <label class="block text-sm font-medium text-gray-200 mb-1">ФИО *</label>
            <input
              v-model="form.fullName"
              type="text"
              required
              class="w-full px-4 py-2 rounded-md border border-white/15 bg-white/5 text-white placeholder:text-gray-500 focus:ring-2 focus:ring-primary-600 focus:border-transparent outline-none"
              placeholder="Иванов Иван Иванович"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-200 mb-1">Email *</label>
            <input
              v-model="form.email"
              type="email"
              required
              class="w-full px-4 py-2 rounded-md border border-white/15 bg-white/5 text-white placeholder:text-gray-500 focus:ring-2 focus:ring-primary-600 focus:border-transparent outline-none"
              placeholder="email@example.com"
            />
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-[#F47427] text-white py-3 rounded-md hover:bg-[#ff8c42] disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors font-semibold"
          >
            {{ loading ? 'Регистрация...' : 'Зарегистрироваться' }}
          </button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-gray-300" v-if="step !== 'phone'">
            <button type="button" @click="resetFlow" class="text-primary-300 hover:text-primary-200 font-semibold">
              Назад
            </button>
          </p>
        </div>
      </div>
    </div>
    <!-- Модальное окно с текстом согласия на обработку персональных данных -->
    <div
      v-if="showPolicyModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4 py-16 md:py-20"
      @click.self="showPolicyModal = false"
    >
      <div class="max-w-xl w-full rounded-2xl bg-[#2E2826] border border-white/10 p-5 md:p-6 text-sm text-gray-100 shadow-xl">
        <h2 class="text-base md:text-lg font-semibold mb-3 text-center">
          Согласие на обработку персональных данных для iHome24.ru
        </h2>
        <div class="space-y-2.5 max-h-[60vh] overflow-y-auto pr-1 text-[11px] md:text-xs leading-relaxed">
          <p>
            1. Настоящим лицо, подтвердившее своё согласие при регистрации на сайте ihome24.ru (далее — Клиент),
            во исполнение требований Федерального закона «О персональных данных» № 152‑ФЗ от 27.07.2006 г.,
            свободно, своей волей и в своём интересе даёт Обществу, осуществляющему деятельность под брендом
            iHome24 (далее — Оператор), а также его обособленным подразделениям на территории Российской Федерации,
            своё согласие на обработку персональных данных, указанных при регистрации и оформлении заказов.
          </p>
          <p>
            2. Обработка персональных данных Клиента осуществляется любыми способами, предусмотренными законодательством РФ,
            включая, но не ограничиваясь: сбор, запись, систематизацию, накопление, хранение, уточнение (обновление,
            изменение), использование, передачу (в том числе третьим лицам по договорам поручения), обезличивание,
            блокирование и уничтожение персональных данных.
          </p>
          <p>
            3. Цели обработки персональных данных: идентификация Клиента; заключение и исполнение договоров купли‑продажи
            и договоров оказания услуг; доставка заказов; ведение взаиморасчётов; улучшение качества работы сайта и
            сервисов; направление информационных и маркетинговых рассылок при наличии согласия.
          </p>
          <p>
            4. Оператор вправе привлекать на договорной основе третьих лиц для обработки персональных данных при условии
            соблюдения ими требований законодательства РФ об обеспечении конфиденциальности и безопасности персональных данных.
          </p>
          <p>
            5. Согласие действует в течение всего срока использования Клиентом сайта и сервисов iHome24 и может быть
            отозвано Клиентом путём направления письменного заявления в адрес Оператора. В случае отзыва согласия обработка
            и хранение персональных данных осуществляется в объёме и в течение сроков, предусмотренных действующим
            законодательством РФ.
          </p>
        </div>
        <div class="mt-4 flex justify-end">
          <button
            type="button"
            class="px-5 py-2.5 rounded-md bg-[#F47427] text-white text-sm font-semibold hover:bg-[#ff8c42] transition-colors"
            @click="showPolicyModal = false"
          >
            Понятно
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
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
const consentChecked = ref(false)
const showPolicyModal = ref(false)

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
    // Простая проверка российского номера: +7 / 7 / 8 и 11 цифр
    const raw = form.value.phone || ''
    const digits = raw.replace(/\D/g, '')
    let normalized = digits
    if (digits.length === 11 && digits.startsWith('8')) {
      normalized = '7' + digits.slice(1)
    }
    if (!(normalized.length === 11 && normalized.startsWith('7'))) {
      error.value = { phone: ['Введите номер телефона РФ в формате +7 9XX XXX-XX-XX'] }
      loading.value = false
      return
    }

    const res = await authApi.sendSmsCode(normalized)
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

watch(showPolicyModal, (val) => {
  if (val) {
    document.body.classList.add('ihome-policy-modal-open')
  } else {
    document.body.classList.remove('ihome-policy-modal-open')
  }
})

onBeforeUnmount(() => {
  document.body.classList.remove('ihome-policy-modal-open')
})
</script>
