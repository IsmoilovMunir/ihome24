<script setup>
import { avatarText } from '@core/utils/formatters'

const orderData = ref(null)
const isLoading = ref(true)
const loadError = ref(null)
const route = useRoute('apps-ecommerce-order-details-id')
const isConfirmDialogVisible = ref(false)
const isUserInfoEditDialogVisible = ref(false)
const isEditAddressDialogVisible = ref(false)

// Редактируемый состав заказа: { productId, productName, price, quantity }
const editableItems = ref([])
const isItemsSaving = ref(false)
const productList = ref([])
const addProductId = ref(null)
const addQuantity = ref(1)
const isProductsLoading = ref(false)

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

// Синхронизируем редактируемый список при загрузке заказа
watch(
  () => orderData.value?.items,
  items => {
    if (!items?.length) {
      editableItems.value = []
      return
    }
    editableItems.value = items.map(item => ({
      productId: item.productId,
      productName: item.productName || '',
      price: item.price != null ? Number(item.price) : 0,
      quantity: item.quantity || 0,
    }))
  },
  { immediate: true },
)

const orderDetail = computed(() =>
  editableItems.value.map(item => ({
    ...item,
    total: item.price * (item.quantity || 0),
  })),
)

const orderTotalFromItems = computed(() =>
  orderDetail.value.reduce((sum, item) => sum + item.total, 0),
)

const fetchProducts = async () => {
  isProductsLoading.value = true
  try {
    const data = await $api('/admin/products', {
      query: { page: 1, itemsPerPage: 200 },
    }).catch(() => ({ products: [] }))
    productList.value = data?.products ?? []
  } catch (e) {
    console.error('Ошибка загрузки товаров:', e)
    productList.value = []
  } finally {
    isProductsLoading.value = false
  }
}

const addProductToOrder = () => {
  const id = addProductId.value
  const qty = Math.max(1, parseInt(addQuantity.value, 10) || 1)
  if (!id) return
  const product = productList.value.find(p => p.id === id)
  if (!product) return
  const existing = editableItems.value.find(i => i.productId === id)
  if (existing) {
    existing.quantity += qty
  } else {
    editableItems.value.push({
      productId: product.id,
      productName: product.name || '',
      price: product.price != null ? Number(product.price) : 0,
      quantity: qty,
    })
  }
  addProductId.value = null
  addQuantity.value = 1
}

const removeItem = index => {
  editableItems.value.splice(index, 1)
}

const saveOrderItems = async () => {
  if (!orderData.value?.id) return
  if (!editableItems.value.length) return
  isItemsSaving.value = true
  try {
    const body = {
      items: editableItems.value.map(i => ({
        productId: i.productId,
        quantity: i.quantity || 1,
      })),
    }
    const data = await $api(`/apps/ecommerce/orders/${orderData.value.id}/items`, {
      method: 'PATCH',
      body,
    })
    orderData.value = { ...orderData.value, ...data }
    editableItems.value = (data.items || []).map(item => ({
      productId: item.productId,
      productName: item.productName || '',
      price: item.price != null ? Number(item.price) : 0,
      quantity: item.quantity || 0,
    }))
  } catch (e) {
    console.error('Ошибка сохранения состава заказа:', e)
  } finally {
    isItemsSaving.value = false
  }
}

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
          <VTable class="text-no-wrap">
            <thead>
              <tr>
                <th class="text-left">
                  Товар
                </th>
                <th class="text-left">
                  Цена
                </th>
                <th class="text-left" style="width: 120px;">
                  Количество
                </th>
                <th class="text-left">
                  Всего
                </th>
                <th class="text-left" style="width: 56px;" />
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(item, idx) in orderDetail"
                :key="item.productId + '-' + idx"
              >
                <td>
                  <h6 class="text-h6 mb-0">
                    {{ item.productName }}
                  </h6>
                </td>
                <td>
                  {{ formatPrice(item.price) }}
                </td>
                <td>
                  <VTextField
                    v-model.number="editableItems[idx].quantity"
                    type="number"
                    min="1"
                    density="compact"
                    hide-details
                    variant="outlined"
                    style="max-width: 80px;"
                    @update:model-value="(v) => { if (v < 1) editableItems[idx].quantity = 1 }"
                  />
                </td>
                <td>
                  {{ formatPrice(item.total) }}
                </td>
                <td>
                  <VBtn
                    icon
                    variant="text"
                    size="small"
                    color="error"
                    @click="removeItem(idx)"
                  >
                    <VIcon icon="tabler-trash" />
                  </VBtn>
                </td>
              </tr>
              <tr>
                <td colspan="5" class="pt-4">
                  <div class="d-flex align-center gap-2 flex-wrap">
                    <VSelect
                      v-model="addProductId"
                      :items="productList"
                      item-title="name"
                      item-value="id"
                      placeholder="Добавить товар"
                      density="compact"
                      style="max-width: 260px;"
                      :loading="isProductsLoading"
                      clearable
                      @focus="productList.length === 0 && fetchProducts()"
                    />
                    <VTextField
                      v-model.number="addQuantity"
                      type="number"
                      min="1"
                      density="compact"
                      hide-details
                      style="max-width: 80px;"
                      placeholder="Кол-во"
                    />
                    <VBtn
                      size="small"
                      color="primary"
                      :disabled="!addProductId"
                      @click="addProductToOrder"
                    >
                      Добавить
                    </VBtn>
                  </div>
                </td>
              </tr>
            </tbody>
          </VTable>
          <VDivider />
          <VCardText>
            <div class="d-flex align-center justify-space-between flex-wrap gap-2">
              <div class="d-flex align-end flex-column">
                <table class="text-high-emphasis">
                  <tbody>
                    <tr>
                      <td class="text-high-emphasis font-weight-medium" width="200">
                        Итого:
                      </td>
                      <td class="font-weight-medium">
                        {{ formatPrice(orderTotalFromItems) }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <VBtn
                color="primary"
                :loading="isItemsSaving"
                :disabled="!editableItems.length"
                @click="saveOrderItems"
              >
                Сохранить состав заказа
              </VBtn>
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
