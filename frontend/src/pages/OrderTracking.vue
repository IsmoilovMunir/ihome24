<template>
  <div class="order-tracking">
    <h1 class="order-tracking__title">Где мой заказ?</h1>

    <p class="order-tracking__hint">
      Введите номер заказа и телефон, чтобы узнать статус.
    </p>

    <form class="order-tracking__form" @submit.prevent="onSubmit">
      <label class="order-tracking__label">
        Номер заказа
        <input
          v-model="orderNumber"
          type="text"
          class="order-tracking__input"
          placeholder="Например, 12345"
          required
        />
      </label>
      <label class="order-tracking__label">
        Номер телефона
        <input
          v-model="phone"
          type="tel"
          inputmode="tel"
          class="order-tracking__input"
          placeholder="+7 9XX XXX XX XX"
          required
        />
      </label>
      <button
        type="submit"
        class="order-tracking__button"
        :disabled="loading || !orderNumber.trim() || !phone.trim()"
      >
        {{ loading ? 'Ищем заказ…' : 'Найти заказ' }}
      </button>
    </form>

    <div v-if="error" class="order-tracking__state order-tracking__state--error">
      {{ error }}
    </div>

    <div
      v-if="order"
      class="order-tracking__card"
    >
      <div class="order-tracking__card-header">
        <div>
          <div class="order-tracking__order-number">
            Заказ #{{ formatOrderNumber(order.order ?? order.id) }}
          </div>
          <div class="order-tracking__order-date">
            {{ formatOrderDate(order.date) }}
          </div>
        </div>
        <div class="order-tracking__status">
          <span
            class="order-tracking__status-badge"
            :class="{ 'order-tracking__status-badge--delivered': isDelivered(order.status) }"
          >
            {{ mapStatus(order.status) }}
          </span>
        </div>
      </div>

      <div class="order-tracking__timeline">
        <div
          v-for="(step, index) in steps"
          :key="step.key"
          class="order-tracking__step"
          :class="{
            'order-tracking__step--done': index < currentStepIndex,
            'order-tracking__step--current': index === currentStepIndex,
          }"
        >
          <div class="order-tracking__step-dot"></div>
          <div class="order-tracking__step-content">
            <div class="order-tracking__step-title">
              {{ step.title }}
            </div>
            <div class="order-tracking__step-desc">
              {{ step.description }}
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="isDelivered(order.status)"
        class="order-tracking__note"
      >
        Заказ доставлен. Отслеживание больше не требуется.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { orderApi } from '../services/api'

const orderNumber = ref('')
const phone = ref('')
const loading = ref(false)
const error = ref(null)
const order = ref(null)

const steps = [
  {
    key: 'created',
    title: 'Заказ оформлен',
    description: 'Мы получили ваш заказ и начали обработку.',
  },
  {
    key: 'processing',
    title: 'Обрабатывается',
    description: 'Проверяем наличие товаров и подготавливаем к отправке.',
  },
  {
    key: 'dispatched',
    title: 'Передан в доставку',
    description: 'Заказ передан курьерской службе или в пункт выдачи.',
  },
  {
    key: 'on_the_way',
    title: 'В пути',
    description: 'Заказ в процессе доставки к вам.',
  },
  {
    key: 'delivered',
    title: 'Доставлен',
    description: 'Заказ получен. Спасибо, что выбрали нас!',
  },
]

const currentStepIndex = ref(0)

const normalizeStatus = (status) => {
  if (!status) return ''
  return String(status).toLowerCase()
}

const isDelivered = (status) => {
  return normalizeStatus(status).includes('delivered')
}

const mapStatus = (status) => {
  const s = normalizeStatus(status)
  if (!s) return 'В обработке'
  if (s.includes('delivered')) return 'Доставлен'
  if (s.includes('pending')) return 'В обработке'
  if (s.includes('in processing') || s.includes('in_process')) return 'Обрабатывается'
  if (s.includes('dispatched')) return 'Отправлен'
  if (s.includes('out for delivery') || s.includes('out_for_delivery')) return 'В доставке'
  if (s.includes('ready to pickup') || s.includes('ready_to_pickup')) return 'Готов к выдаче'
  if (s.includes('cancel')) return 'Отменён'
  if (s.includes('failed')) return 'Ошибка оплаты'
  return status
}

const getStepIndexByStatus = (status) => {
  const s = normalizeStatus(status)
  if (!s) return 0
  if (s.includes('delivered')) return 4
  if (s.includes('out for delivery') || s.includes('out_for_delivery')) return 3
  if (s.includes('dispatched')) return 2
  if (s.includes('in processing') || s.includes('in_process')) return 1
  if (s.includes('pending')) return 1
  return 0
}

const formatOrderDate = (value) => {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return new Intl.DateTimeFormat('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(d)
}

const formatOrderNumber = (value) => {
  if (value == null) return '—'
  const n = Number(value)
  if (!Number.isFinite(n)) return String(value)
  return n.toString().padStart(3, '0')
}

const normalizePhone = (phone) => {
  if (!phone) return ''
  const digits = String(phone).replace(/\D/g, '')
  return digits.slice(-10)
}

const phonesMatch = (a, b) => {
  const na = normalizePhone(a)
  const nb = normalizePhone(b)
  if (!na || !nb) return false
  return na === nb
}

const extractOrderContacts = (data) => {
  if (!data) return { email: null, phone: null }
  const email =
    data.email ??
    data.customerEmail ??
    data.userEmail ??
    null

  const phone =
    data.phone ??
    data.customerPhone ??
    data.userPhone ??
    null

  return { email, phone }
}

const onSubmit = async () => {
  if (!orderNumber.value.trim() || !phone.value.trim()) return

  loading.value = true
  error.value = null
  order.value = null

  try {
    const response = await orderApi.getById(orderNumber.value.trim())
    const data = response.data
    const { phone: orderPhone } = extractOrderContacts(data)

    if (!orderPhone || !phonesMatch(orderPhone, phone.value)) {
      error.value = 'Не удалось найти заказ по этому номеру телефона. Проверьте данные и попробуйте ещё раз.'
      order.value = null
      return
    }

    order.value = data
    currentStepIndex.value = getStepIndexByStatus(data.status)
  } catch (err) {
    console.error('Order tracking error:', err)
    error.value =
      err.response?.data?.message ||
      err.response?.data?.error ||
      'Не удалось найти заказ. Проверьте номер и попробуйте ещё раз.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.order-tracking {
  font-family: "helvetica", sans-serif;
  max-width: 640px;
  margin: 0 auto;
}

.order-tracking__title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.order-tracking__hint {
  margin: 0 0 20px;
  font-size: 14px;
  color: #9d9390;
}

.order-tracking__form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 20px;
}

.order-tracking__label {
  flex: 1;
  min-width: 180px;
  font-size: 13px;
  color: #d1d5db;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-tracking__input {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(255, 255, 255, 0.04);
  color: #fff;
  font-size: 14px;
  outline: none;
}

.order-tracking__input::placeholder {
  color: #6b615e;
}

.order-tracking__input:focus {
  border-color: rgba(244, 116, 39, 0.6);
}

.order-tracking__button {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  background: #F47427;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s, opacity 0.2s;
}

.order-tracking__button:hover:enabled {
  background: #ff8c42;
}

.order-tracking__button:disabled {
  opacity: 0.7;
  cursor: default;
}

.order-tracking__state {
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  background: #2e2826;
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e5e7eb;
}

.order-tracking__state--error {
  border-color: rgba(248, 113, 113, 0.5);
  color: #fecaca;
}

.order-tracking__card {
  margin-top: 16px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #2e2826;
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-tracking__card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.order-tracking__order-number {
  font-size: 16px;
  font-weight: 600;
  color: #F47427;
}

.order-tracking__order-date {
  font-size: 13px;
  color: #9d9390;
}

.order-tracking__status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(244, 116, 39, 0.12);
  color: #f47427;
}

.order-tracking__status-badge--delivered {
  background: rgba(22, 163, 74, 0.85);
  color: #ffffff;
}

.order-tracking__timeline {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-tracking__step {
  display: flex;
  gap: 10px;
  position: relative;
  padding-left: 4px;
}

.order-tracking__step::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 14px;
  bottom: -10px;
  width: 2px;
  background: rgba(156, 163, 175, 0.3);
}

.order-tracking__step:last-child::before {
  display: none;
}

.order-tracking__step-dot {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 2px solid #6b7280;
  background: #2e2826;
  flex-shrink: 0;
  margin-top: 3px;
}

.order-tracking__step--done .order-tracking__step-dot {
  border-color: #16a34a;
  background: #16a34a;
}

.order-tracking__step--current .order-tracking__step-dot {
  border-color: #F47427;
  background: #F47427;
}

.order-tracking__step-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-tracking__step-title {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
}

.order-tracking__step-desc {
  font-size: 12px;
  color: #9d9390;
}

.order-tracking__step--done .order-tracking__step-title {
  color: #d1fae5;
}

.order-tracking__step--current .order-tracking__step-title {
  color: #fed7aa;
}

.order-tracking__note {
  margin-top: 4px;
  padding-top: 6px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 13px;
  color: #9d9390;
}

@media (max-width: 600px) {
  .order-tracking__form {
    flex-direction: column;
    align-items: stretch;
  }

  .order-tracking__button {
    width: 100%;
    justify-content: center;
    text-align: center;
  }
}
</style>

