<script setup>
import { avatarText } from '@core/utils/formatters'
import masterCardDark from '@images/icons/payments/img/master-dark.png'
import masterCardLight from '@images/icons/payments/img/mastercard.png'
import paypalDark from '@images/icons/payments/img/paypal-dark.png'
import paypalLight from '@images/icons/payments/img/paypal-light.png'

definePage({
  meta: {
    action: 'manage',
    subject: 'Ecommerce',
    navActiveLink: 'apps-ecommerce-order-preliminary',
  },
})

const widgetData = ref([
  { title: 'Предварительные', value: 0, icon: 'tabler-clock-pause' },
])

const fetchOrderStats = async () => {
  try {
    const data = await $api('/apps/ecommerce/orders/stats')
    widgetData.value = [
      { title: 'Предварительные', value: data?.preliminary ?? 0, icon: 'tabler-clock-pause' },
    ]
  } catch (e) {
    console.error('Ошибка загрузки статистики:', e)
  }
}

const mastercard = useGenerateImageVariant(masterCardLight, masterCardDark)
const paypal = useGenerateImageVariant(paypalLight, paypalDark)
const searchQuery = ref('')
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref()
const orderBy = ref()
const selectedRows = ref([])

const headers = [
  { title: 'Заказ', key: 'order' },
  { title: 'Дата', key: 'date' },
  { title: 'Клиенты', key: 'customers' },
  { title: 'Оплата', key: 'payment', sortable: false },
  { title: 'Статус', key: 'status' },
  { title: 'Способ', key: 'method', sortable: false },
  { title: 'Действие', key: 'actions', sortable: false },
]

const updateOptions = options => {
  sortBy.value = options.sortBy[0]?.key
  orderBy.value = options.sortBy[0]?.order
}

const ordersData = ref({ orders: [], total: 0 })
const isLoading = ref(false)

const fetchOrders = useDebounceFn(async () => {
  isLoading.value = true
  try {
    const params = new URLSearchParams()
    if (searchQuery.value) params.set('q', searchQuery.value)
    params.set('page', String(page.value))
    params.set('itemsPerPage', String(itemsPerPage.value))
    if (sortBy.value) params.set('sortBy', sortBy.value)
    if (orderBy.value) params.set('orderBy', orderBy.value)
    params.set('preliminary', 'true')
    const data = await $api(`/apps/ecommerce/orders?${params}`)
    ordersData.value = { orders: data?.orders ?? [], total: data?.total ?? 0 }
  } catch (e) {
    console.error('Ошибка загрузки предварительных заказов:', e)
    ordersData.value = { orders: [], total: 0 }
  } finally {
    isLoading.value = false
  }
}, 150)

onMounted(() => {
  fetchOrders()
  fetchOrderStats()
})

watch([searchQuery, page, itemsPerPage, sortBy, orderBy], () => {
  fetchOrders()
})

const resolvePaymentStatus = status => {
  if (status === 1) return { text: 'Оплачено', color: 'success' }
  if (status === 2) return { text: 'Ожидает', color: 'warning' }
  if (status === 3) return { text: 'Отменено', color: 'secondary' }
  if (status === 4) return { text: 'Неудачно', color: 'error' }
}

const resolveStatus = status => {
  if (status === 'Preliminary') return { text: 'Предварительный', color: 'secondary' }
  return { text: String(status || '—'), color: 'secondary' }
}

const orders = computed(() => ordersData.value?.orders ?? [])
const totalOrder = computed(() => ordersData.value?.total ?? 0)

const deleteOrder = async id => {
  await $api(`/apps/ecommerce/orders/${id}`, { method: 'DELETE' })
  const index = selectedRows.value.findIndex(row => row === id)
  if (index !== -1) selectedRows.value.splice(index, 1)
  fetchOrders()
  fetchOrderStats()
}
</script>

<template>
  <motion.div
    :initial="{ opacity: 0 }"
    :animate="{ opacity: 1 }"
    :transition="{ duration: 0.3 }"
  >
    <div class="d-flex align-center gap-2 mb-4">
      <VBtn
        variant="text"
        :to="{ name: 'apps-ecommerce-order-list' }"
        prepend-icon="tabler-arrow-left"
      >
        Активные заказы
      </VBtn>
    </div>

    <VCard class="mb-6">
      <VCardText>
        <VRow>
          <VCol
            cols="12"
            md="4"
          >
            <div class="d-flex justify-space-between">
              <motion.div
                class="d-flex flex-column"
                :initial="{ opacity: 0, y: 6 }"
                :animate="{ opacity: 1, y: 0 }"
                :transition="{ duration: 0.25 }"
              >
                <h4 class="text-h4">
                  {{ widgetData[0]?.value ?? 0 }}
                </h4>
                <motion.div
                  class="text-body-1"
                  :initial="{ opacity: 0 }"
                  :animate="{ opacity: 1 }"
                  :transition="{ duration: 0.2, delay: 0.08 }"
                >
                  Предварительные
                </motion.div>
              </motion.div>
              <motion.div
                :initial="{ scale: 0.92, opacity: 0 }"
                :animate="{ scale: 1, opacity: 1 }"
                :transition="{ duration: 0.25, delay: 0.05 }"
              >
                <VAvatar
                  variant="tonal"
                  rounded
                  size="42"
                >
                  <VIcon
                    icon="tabler-clock-pause"
                    size="26"
                    class="text-high-emphasis"
                  />
                </VAvatar>
              </motion.div>
            </div>
          </VCol>
        </VRow>
      </VCardText>
    </VCard>

    <VCard>
      <VCardTitle class="px-4 pt-4">
        Предварительные заказы
      </VCardTitle>
      <VCardSubtitle class="px-4 pb-2">
        Клиент указал контакты, но не завершил оформление. После оплаты заказ появится в «Список».
      </VCardSubtitle>

      <VCardText>
        <div class="d-flex justify-sm-space-between justify-start flex-wrap gap-4">
          <AppTextField
            v-model="searchQuery"
            placeholder="Поиск заказа"
            style="max-inline-size: 240px; min-inline-size: 200px;"
          />
          <AppSelect
            :model-value="itemsPerPage"
            style="min-inline-size: 6.25rem;"
            :items="[5, 10, 20, 50].map(n => ({ value: n, title: String(n) }))"
            item-value="value"
            item-title="title"
            @update:model-value="itemsPerPage = Number($event)"
          />
        </div>
      </VCardText>

      <VDivider />

      <VDataTableServer
        v-model:items-per-page="itemsPerPage"
        v-model:model-value="selectedRows"
        v-model:page="page"
        :headers="headers"
        :items="orders"
        :items-length="totalOrder"
        :loading="isLoading"
        show-select
        class="text-no-wrap"
        @update:options="updateOptions"
      >
        <template #item.order="{ item }">
          <RouterLink :to="{ name: 'apps-ecommerce-order-details-id', params: { id: item.id } }">
            #{{ item.order }}
          </RouterLink>
        </template>
        <template #item.date="{ item }">
          {{ new Date(item.date).toDateString() }}
        </template>
        <template #item.customers="{ item }">
          <div class="d-flex align-center gap-x-3">
            <VAvatar
              size="34"
              :color="!item.avatar?.length ? 'primary' : ''"
              :variant="!item.avatar?.length ? 'tonal' : undefined"
            >
              <span class="font-weight-medium">{{ avatarText(item.customer) }}</span>
            </VAvatar>
            <motion.div
              class="d-flex flex-column"
              :initial="{ opacity: 0 }"
              :animate="{ opacity: 1 }"
              :transition="{ duration: 0.2 }"
            >
              <div class="text-body-1 font-weight-medium">
                <OrderCustomerLink :item="item" />
              </div>
              <motion.div
                class="text-body-2"
                :initial="{ opacity: 0 }"
                :animate="{ opacity: 1 }"
                :transition="{ duration: 0.2, delay: 0.05 }"
              >
                {{ item.email }}
              </motion.div>
            </motion.div>
          </div>
        </template>
        <template #item.payment="{ item }">
          <div
            :class="`text-${resolvePaymentStatus(item.payment)?.color}`"
            class="font-weight-medium d-flex align-center gap-x-2"
          >
            <VIcon
              icon="tabler-circle-filled"
              size="10"
            />
            <div style="line-height: 22px;">
              {{ resolvePaymentStatus(item.payment)?.text }}
            </div>
          </div>
        </template>
        <template #item.status="{ item }">
          <VChip
            v-bind="resolveStatus(item.status)"
            label
            size="small"
          />
        </template>
        <template #item.method="{ item }">
          <span class="text-body-2 text-medium-emphasis">—</span>
        </template>
        <template #item.actions="{ item }">
          <IconBtn>
            <VIcon icon="tabler-dots-vertical" />
            <VMenu activator="parent">
              <VList>
                <VListItem :to="{ name: 'apps-ecommerce-order-details-id', params: { id: item.id } }">
                  Открыть
                </VListItem>
                <VListItem @click="deleteOrder(item.id)">
                  Удалить
                </VListItem>
              </VList>
            </VMenu>
          </IconBtn>
        </template>
        <template #bottom>
          <TablePagination
            v-model:page="page"
            :items-per-page="itemsPerPage"
            :total-items="totalOrder"
          />
        </template>
      </VDataTableServer>
    </VCard>
  </motion.div>
</template>
