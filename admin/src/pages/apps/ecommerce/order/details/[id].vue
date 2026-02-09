<script setup>
import { avatarText } from '@core/utils/formatters'

const orderData = ref(null)
const isLoading = ref(true)
const loadError = ref(null)
const route = useRoute('apps-ecommerce-order-details-id')
const isConfirmDialogVisible = ref(false)
const isUserInfoEditDialogVisible = ref(false)
const isEditAddressDialogVisible = ref(false)

const headers = [
  {
    title: 'Товар',
    key: 'productName',
  },
  {
    title: 'Цена',
    key: 'price',
  },
  {
    title: 'Количество',
    key: 'quantity',
  },
  {
    title: 'Всего',
    key: 'total',
  },
]

const resolvePaymentStatus = payment => {
  if (payment === 1)
    return { text: 'Оплачено', color: 'success' }
  if (payment === 2)
    return { text: 'Ожидает', color: 'warning' }
  if (payment === 3)
    return { text: 'Отменено', color: 'secondary' }
  if (payment === 4)
    return { text: 'Ошибка', color: 'error' }
}

const resolveStatus = status => {
  if (status === 'Pending')
    return { text: 'Ожидает', color: 'warning' }
  if (status === 'In Processing')
    return { text: 'В обработке', color: 'info' }
  if (status === 'Delivered')
    return { text: 'Доставлено', color: 'success' }
  if (status === 'Out for Delivery')
    return { text: 'В доставке', color: 'primary' }
  if (status === 'Ready to Pickup')
    return { text: 'Готово к выдаче', color: 'info' }
  if (status === 'Dispatched')
    return { text: 'Отправлено', color: 'warning' }
  return { text: String(status || ''), color: 'secondary' }
}

// API принимает: PENDING, IN_PROCESSING, DISPATCHED, OUT_FOR_DELIVERY, READY_TO_PICKUP, DELIVERED
const statusDisplayToApi = {
  Pending: 'PENDING',
  'In Processing': 'IN_PROCESSING',
  Dispatched: 'DISPATCHED',
  'Out for Delivery': 'OUT_FOR_DELIVERY',
  'Ready to Pickup': 'READY_TO_PICKUP',
  Delivered: 'DELIVERED',
}
const statusOptions = [
  { value: 'PENDING', title: 'Ожидает' },
  { value: 'IN_PROCESSING', title: 'В обработке' },
  { value: 'DISPATCHED', title: 'Отправлено' },
  { value: 'OUT_FOR_DELIVERY', title: 'В доставке' },
  { value: 'READY_TO_PICKUP', title: 'Готово к выдаче' },
  { value: 'DELIVERED', title: 'Доставлено' },
]
const selectedStatus = computed(() => {
  const s = orderData.value?.status
  if (!s) return 'PENDING'
  return statusDisplayToApi[s] || 'PENDING'
})
const isStatusUpdating = ref(false)
const handleStatusChange = async newStatus => {
  if (!orderData.value?.id || !newStatus) return
  isStatusUpdating.value = true
  try {
    const data = await $api(`/apps/ecommerce/orders/${orderData.value.id}/status`, {
      method: 'PATCH',
      body: { status: newStatus },
    })
    orderData.value = { ...orderData.value, ...data }
  } catch (e) {
    console.error('Ошибка смены статуса:', e)
  } finally {
    isStatusUpdating.value = false
  }
}

const orderDetail = computed(() => {
  if (!orderData.value?.items?.length)
    return []
  return orderData.value.items.map(item => ({
    productName: item.productName || '',
    price: item.price != null ? Number(item.price) : 0,
    quantity: item.quantity || 0,
    total: item.total != null ? Number(item.total) : 0,
  }))
})

const formatPrice = val => {
  if (val == null) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 }).format(val)
}

const formatDate = dateStr => {
  if (!dateStr) return ''
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })
  } catch {
    return dateStr
  }
}

const userData = computed(() => ({
  id: null,
  fullName: orderData.value?.customer || '',
  email: orderData.value?.email || '',
  company: '',
  role: '',
  username: '',
  country: 'Россия',
  contact: orderData.value?.phone || '',
  status: 'Active',
  taxId: '',
  language: '',
  currentPlan: '',
  avatar: '',
  taskDone: null,
  projectDone: null,
}))

const currentBillingAddress = computed(() => ({
  fullName: orderData.value?.customer || '',
  firstName: (orderData.value?.customer || '').split(' ')[0] || '',
  lastName: (orderData.value?.customer || '').split(' ')[1] || '',
  selectedCountry: 'Россия',
  addressLine1: orderData.value?.address || '',
  addressLine2: '',
  landmark: '',
  contact: orderData.value?.phone || '',
  country: 'Россия',
  city: '',
  state: '',
  zipCode: '',
}))

const fetchOrder = async () => {
  isLoading.value = true
  loadError.value = null
  try {
    const data = await $api(`/apps/ecommerce/orders/${route.params.id}`)
    orderData.value = data
  } catch (e) {
    console.error('Ошибка загрузки заказа:', e)
    const msg = e?.data?.message ?? e?.message ?? e?.statusMessage ?? 'Заказ не найден'
    loadError.value = msg
    orderData.value = null
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchOrder)
watch(() => route.params.id, fetchOrder)

const handleDeleteOrder = async () => {
  if (!orderData.value?.id) return
  try {
    await $api(`/apps/ecommerce/orders/${orderData.value.id}`, { method: 'DELETE' })
    isConfirmDialogVisible.value = false
    navigateTo('/apps/ecommerce/order/list')
  } catch (e) {
    console.error('Ошибка удаления:', e)
  }
}
</script>

<template>
  <div v-if="isLoading" class="d-flex justify-center py-12">
    <VProgressCircular indeterminate />
  </div>

  <div v-else-if="orderData" class="mb-6">
    <div class="d-flex justify-space-between align-center flex-wrap gap-y-4 mb-6">
      <div>
        <div class="d-flex gap-2 align-center mb-2 flex-wrap">
          <h5 class="text-h5">
            Заказ #{{ orderData.order }}
          </h5>
          <div class="d-flex gap-x-2">
            <VChip
              v-if="orderData.payment != null"
              variant="tonal"
              :color="resolvePaymentStatus(orderData.payment)?.color"
              label
              size="small"
            >
              {{ resolvePaymentStatus(orderData.payment)?.text }}
            </VChip>
            <VChip
              v-if="orderData.status"
              v-bind="resolveStatus(orderData.status)"
              label
              size="small"
            />
          </div>
        </div>
        <div class="text-body-1">
          {{ formatDate(orderData.date) }}
        </div>
      </div>

      <VBtn
        variant="tonal"
        color="error"
        @click="isConfirmDialogVisible = true"
      >
        Удалить заказ
      </VBtn>
    </div>

    <VRow>
      <VCol cols="12" md="8">
        <VCard class="mb-6">
          <VCardItem>
            <template #title>
              <h5 class="text-h5">Детали заказа</h5>
            </template>
          </VCardItem>
          <VDivider />
          <VDataTable
            :headers="headers"
            :items="orderDetail"
            item-value="productName"
            class="text-no-wrap"
          >
            <template #item.productName="{ item }">
              <div class="d-flex flex-column align-start">
                <h6 class="text-h6">
                  {{ item.productName }}
                </h6>
              </div>
            </template>
            <template #item.price="{ item }">
              {{ formatPrice(item.price) }}
            </template>
            <template #item.total="{ item }">
              {{ formatPrice(item.total) }}
            </template>
            <template #item.quantity="{ item }">
              {{ item.quantity }}
            </template>
            <template #bottom />
          </VDataTable>
          <VDivider />
          <VCardText>
            <div class="d-flex align-end flex-column">
              <table class="text-high-emphasis">
                <tbody>
                  <tr>
                    <td class="text-high-emphasis font-weight-medium" width="200">
                      Итого:
                    </td>
                    <td class="font-weight-medium">
                      {{ formatPrice(orderData.spent) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </VCardText>
        </VCard>
      </VCol>

      <VCol cols="12" md="4">
        <VCard class="mb-6">
          <VCardItem>
            <VCardTitle>Статус заказа</VCardTitle>
          </VCardItem>
          <VCardText>
            <AppSelect
              :model-value="selectedStatus"
              :items="statusOptions"
              item-value="value"
              item-title="title"
              :loading="isStatusUpdating"
              placeholder="Выберите статус"
              @update:model-value="handleStatusChange"
            />
            <p class="text-body-2 text-medium-emphasis mt-2 mb-0">
              Ожидает → В обработке → Отправлено → Доставлено
            </p>
          </VCardText>
        </VCard>

        <VCard class="mb-6">
          <VCardText class="d-flex flex-column gap-y-6">
            <h5 class="text-h5">Данные клиента</h5>
            <div class="d-flex align-center">
              <VAvatar
                :variant="!orderData.avatar ? 'tonal' : undefined"
                :rounded="1"
                class="me-3"
              >
                <VImg v-if="orderData.avatar" :src="orderData.avatar" />
                <span v-else class="font-weight-medium">{{ avatarText(orderData.customer) }}</span>
              </VAvatar>
              <div>
                <h6 class="text-h6">
                  {{ orderData.customer }}
                </h6>
                <div class="text-body-1">
                  Заказ #{{ orderData.order }}
                </div>
              </div>
            </div>
            <div class="d-flex flex-column gap-y-1">
              <h6 class="text-h6">Контактная информация</h6>
              <span>Email: {{ orderData.email }}</span>
              <span v-if="orderData.phone">Телефон: {{ orderData.phone }}</span>
            </div>
          </VCardText>
        </VCard>

        <VCard class="mb-6">
          <VCardItem>
            <VCardTitle>Адрес доставки</VCardTitle>
          </VCardItem>
          <VCardText>
            <div class="text-body-1">
              {{ orderData.address || '—' }}
            </div>
          </VCardText>
        </VCard>

        <VCard>
          <VCardText>
            <h5 class="text-h5 mb-1">Способ оплаты</h5>
            <div class="text-body-1">
              {{ orderData.method === 'cash' ? 'Наличные при получении' : orderData.method || '—' }}
            </div>
          </VCardText>
        </VCard>
      </VCol>
    </VRow>

    <ConfirmDialog
      v-model:is-dialog-visible="isConfirmDialogVisible"
      confirmation-question="Вы уверены, что хотите удалить заказ?"
      cancel-title="Отмена"
      cancel-msg="Заказ не удален."
      confirm-title="Удалено"
      confirm-msg="Заказ успешно удален."
      @confirm="(ok) => ok && handleDeleteOrder()"
    />
  </div>

  <section v-else>
    <VAlert type="error" variant="tonal">
      {{ loadError || `Заказ #${route.params.id} не найден или недоступен!` }}
    </VAlert>
  </section>
</template>
