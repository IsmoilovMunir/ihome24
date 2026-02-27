<template>
  <div class="personal-orders">
    <h1 class="personal-orders__title">Мои заказы</h1>

    <div v-if="!authStore.isAuthenticated" class="personal-orders__empty">
      <p class="personal-orders__empty-text">Войдите в аккаунт, чтобы увидеть свои заказы.</p>
      <router-link to="/login" class="personal-orders__link">Войти</router-link>
    </div>

    <div v-else>
      <p class="personal-orders__hint">
        Показываем заказы, оформленные на ваши контактные данные:
        <span v-if="authStore.user?.phone">{{ formatPhone(authStore.user.phone) }}</span>
        <span v-if="authStore.user?.phone && authStore.user?.email"> · </span>
        <span v-if="authStore.user?.email">{{ authStore.user.email }}</span>
      </p>

      <div v-if="loading" class="personal-orders__state">
        <span>Загружаем ваши заказы...</span>
      </div>

      <div v-else-if="error" class="personal-orders__state personal-orders__state--error">
        <span>{{ error }}</span>
      </div>

      <div v-else-if="!orders.length" class="personal-orders__state">
        <span>У вас пока нет заказов.</span>
        <router-link to="/products" class="personal-orders__link">Перейти в каталог</router-link>
      </div>

      <div v-else>
        <div v-if="activeOrders.length" class="personal-orders__section">
          <h2 class="personal-orders__section-title">Текущие заказы</h2>
          <div class="personal-orders__list">
            <article
              v-for="order in activeOrders"
              :key="order.id"
              class="personal-orders__item"
            >
              <div class="personal-orders__item-header">
                <div>
                  <div class="personal-orders__order-number">Заказ #{{ formatOrderNumber(order.order ?? order.id) }}</div>
                  <div class="personal-orders__order-date">{{ formatOrderDate(order.date) }}</div>
                </div>
                <div class="personal-orders__status">
                  <span
                    class="personal-orders__status-badge"
                    :class="{ 'personal-orders__status-badge--delivered': isDelivered(order.status) }"
                  >
                    {{ mapStatus(order.status) }}
                  </span>
                </div>
              </div>

              <div class="personal-orders__item-body">
                <div
                  v-if="order.items && order.items.length"
                  class="personal-orders__products"
                >
                  <router-link
                    v-for="item in order.items"
                    :key="getProductKey(item)"
                    :to="getProductLink(item)"
                    class="personal-orders__product"
                  >
                    <div class="personal-orders__product-image-wrapper">
                      <img
                        v-if="getProductImage(item)"
                        :src="getProductImage(item)"
                        :alt="getProductName(item)"
                        class="personal-orders__product-image"
                      />
                      <div v-else class="personal-orders__product-image-placeholder">
                        <svg class="personal-orders__product-image-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                    </div>
                    <div class="personal-orders__product-info">
                      <div class="personal-orders__product-name">
                        {{ getProductName(item) }}
                      </div>
                      <div class="personal-orders__product-meta">
                        {{ getProductQuantity(item) }} шт.
                      </div>
                    </div>
                  </router-link>
                </div>
                <div class="personal-orders__row">
                  <span class="personal-orders__label">Сумма</span>
                  <span class="personal-orders__value">
                    {{ formatPrice(order.spent) }}
                  </span>
                </div>
                <div class="personal-orders__row">
                  <span class="personal-orders__label">Способ оплаты</span>
                  <span class="personal-orders__value">
                    {{ mapPaymentMethod(order.method) }}
                  </span>
                </div>
              </div>
            </article>
          </div>
        </div>

        <div v-if="completedOrders.length" class="personal-orders__section personal-orders__section--completed">
          <h2 class="personal-orders__section-title">Завершённые заказы</h2>
          <div class="personal-orders__list">
            <article
              v-for="order in completedOrders"
              :key="order.id"
              class="personal-orders__item"
            >
              <div class="personal-orders__item-header">
                <div>
                  <div class="personal-orders__order-number">Заказ #{{ formatOrderNumber(order.order ?? order.id) }}</div>
                  <div class="personal-orders__order-date">{{ formatOrderDate(order.date) }}</div>
                </div>
                <div class="personal-orders__status">
                  <span
                    class="personal-orders__status-badge personal-orders__status-badge--delivered"
                  >
                    {{ mapStatus(order.status) }}
                  </span>
                </div>
              </div>

              <div class="personal-orders__item-body">
                <div
                  v-if="order.items && order.items.length"
                  class="personal-orders__products"
                >
                  <router-link
                    v-for="item in order.items"
                    :key="getProductKey(item)"
                    :to="getProductLink(item)"
                    class="personal-orders__product"
                  >
                    <div class="personal-orders__product-image-wrapper">
                      <img
                        v-if="getProductImage(item)"
                        :src="getProductImage(item)"
                        :alt="getProductName(item)"
                        class="personal-orders__product-image"
                      />
                      <div v-else class="personal-orders__product-image-placeholder">
                        <svg class="personal-orders__product-image-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                    </div>
                    <div class="personal-orders__product-info">
                      <div class="personal-orders__product-name">
                        {{ getProductName(item) }}
                      </div>
                      <div class="personal-orders__product-meta">
                        {{ getProductQuantity(item) }} шт.
                      </div>
                    </div>
                  </router-link>
                </div>
                <div class="personal-orders__row">
                  <span class="personal-orders__label">Сумма</span>
                  <span class="personal-orders__value">
                    {{ formatPrice(order.spent) }}
                  </span>
                </div>
                <div class="personal-orders__row">
                  <span class="personal-orders__label">Способ оплаты</span>
                  <span class="personal-orders__value">
                    {{ mapPaymentMethod(order.method) }}
                  </span>
                </div>
              </div>
            </article>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { orderApi, fileApi } from '../services/api'

const authStore = useAuthStore()

const loading = ref(false)
const error = ref(null)
const orders = ref([])

const activeOrders = computed(() => orders.value.filter(o => !isDelivered(o.status)))
const completedOrders = computed(() => orders.value.filter(o => isDelivered(o.status)))

const formatPhone = (phone) => {
  if (!phone) return ''
  const digits = phone.replace(/\D/g, '')
  if (digits.length >= 10) {
    const d = digits.slice(-10)
    return `+7 (${d.slice(0, 3)}) ${d.slice(3, 6)}-${d.slice(6, 8)}-${d.slice(8, 10)}`
  }
  return phone
}

const formatPrice = (value) => {
  if (value == null) return '—'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
  }).format(value)
}

const formatOrderDate = (value) => {
  if (!value) return ''
  // Бэкенд отдаёт строку вида M/d/yyyy, парсим через Date
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

const mapStatus = (status) => {
  if (!status) return 'В обработке'
  const s = status.toLowerCase()
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

const isDelivered = (status) => {
  if (!status) return false
  return String(status).toLowerCase().includes('delivered')
}

const mapPaymentMethod = (method) => {
  if (!method) return 'Не указан'
  const m = String(method).toLowerCase()
  if (m.includes('cash') || m === 'наличными') return 'Наличными / картой'
  if (m.includes('mastercard') || m.includes('card')) return 'Картой'
  if (m.includes('paypal')) return 'PayPal'
  return method
}

const getProductId = (item) => {
  if (!item) return null
  return (
    item.productId ??
    item.id ??
    item.product?.id ??
    item.product?.productId ??
    null
  )
}

const getProductKey = (item) => {
  return (
    getProductId(item) ??
    item?.sku ??
    item?.productName ??
    item?.name ??
    Math.random()
  )
}

const getProductName = (item) => {
  if (!item) return 'Товар'
  return (
    item.productName ??
    item.name ??
    item.title ??
    item.product?.name ??
    item.product?.title ??
    'Товар'
  )
}

const getProductQuantity = (item) => {
  if (!item) return 1
  return item.quantity ?? item.qty ?? item.count ?? 1
}

const getProductImage = (item) => {
  if (!item) return null

  const rawPath =
    item.imageUrl ??
    item.image ??
    item.imagePath ??
    item.product?.imageUrl ??
    item.product?.image ??
    item.product?.imagePath ??
    null

  if (!rawPath) return null
  return fileApi.getFileUrl(rawPath)
}

const getProductLink = (item) => {
  const id = getProductId(item)
  return id ? `/products/${id}` : '#'
}

const fetchOrders = async () => {
  if (!authStore.isAuthenticated) return

  loading.value = true
  error.value = null
  try {
    // Бэкенд возвращает только заказы текущего пользователя (по токену, по email/телефону)
    const response = await orderApi.listMy({
      page: 1,
      itemsPerPage: 20,
      sortBy: 'date',
      orderBy: 'desc',
    })
    const baseOrders = response.data?.orders || []

    // Для каждого заказа подтягиваем детали (включая список товаров)
    const detailed = await Promise.all(
      baseOrders.map(async (o) => {
        try {
          const detailRes = await orderApi.getById(o.id ?? o.order)
          return detailRes.data ? { ...o, ...detailRes.data } : o
        } catch (e) {
          console.error('Failed to load order details', o.id, e)
          return o
        }
      })
    )

    orders.value = detailed
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data?.error || 'Не удалось загрузить список заказов.'
    console.error('Personal orders error:', err)
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.personal-orders {
  font-family: "helvetica", sans-serif;
  color: #fff;
}

.personal-orders__title {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 600;
}

.personal-orders__hint {
  margin: 0 0 16px;
  font-size: 14px;
  color: #9d9390;
}

.personal-orders__state {
  padding: 24px;
  border-radius: 12px;
  background: #2e2826;
  border: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 14px;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.personal-orders__state--error {
  border-color: rgba(248, 113, 113, 0.5);
  color: #fecaca;
}

.personal-orders__link {
  color: #f47427;
  font-weight: 500;
  text-decoration: none;
}

.personal-orders__link:hover {
  text-decoration: underline;
}

.personal-orders__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.personal-orders__section {
  margin-bottom: 20px;
}

.personal-orders__section-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.personal-orders__section--completed .personal-orders__section-title {
  color: #9d9390;
}

.personal-orders__item {
  background: #2e2826;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.personal-orders__item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.personal-orders__order-number {
  font-size: 16px;
  font-weight: 600;
}

.personal-orders__order-date {
  font-size: 13px;
  color: #9d9390;
}

.personal-orders__status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(244, 116, 39, 0.12);
  color: #f47427;
}

.personal-orders__status-badge--delivered {
  background: rgba(22, 163, 74, 0.85);
  color: #ffffff;
}

.personal-orders__item-body {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 14px;
}

.personal-orders__products {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.personal-orders__product {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
}

.personal-orders__product-image-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  background: #26211e;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.personal-orders__product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.personal-orders__product-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9d9390;
}

.personal-orders__product-image-icon {
  width: 22px;
  height: 22px;
}

.personal-orders__product-info {
  flex: 1;
  min-width: 0;
}

.personal-orders__product-name {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
}

.personal-orders__product-meta {
  font-size: 12px;
  color: #9d9390;
}

.personal-orders__row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.personal-orders__label {
  color: #9d9390;
}

.personal-orders__value {
  font-weight: 500;
}

.personal-orders__empty {
  padding: 24px;
  border-radius: 12px;
  background: #2e2826;
  border: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 14px;
  color: #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.personal-orders__empty-text {
  margin: 0;
}
</style>

